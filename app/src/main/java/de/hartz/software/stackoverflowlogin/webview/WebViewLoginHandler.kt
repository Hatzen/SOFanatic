package de.hartz.software.stackoverflowlogin.webview

import android.content.Context
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import de.hartz.software.stackoverflowlogin.helper.Helper
import de.hartz.software.stackoverflowlogin.helper.PersistenceHelper
import kotlin.random.Random
import kotlin.reflect.KFunction

class WebViewLoginHandler(val context: Context, val webView: WebView, val onlyLogin: Boolean = false): WebViewClient() {
    companion object {
        val ANDROID_CALLBACK = "ANDROID_CALLBACK"
    }

    private val JS_EXPRESSION_SUCCESSFUL_LOGIN = "document.getElementsByClassName('js-inbox-button').length > 0"
    private var loginTrialsCounter = 0
    val scripts: MutableList<KFunction<String>>

    val continueAfterPageReload: MutableList<String>

    init {
        continueAfterPageReload = mutableListOf()
        scripts =
            mutableListOf(
                ::getJSLoginCode,
                ::getJSLoginAndroidCallback,
                ::getJSGetBadgeCallback
            )
    }

    fun commandFinished() {
        if (onlyLogin == true) {
            return
        }
        android.os.Handler().postDelayed( object: Runnable {

            override fun run() {
                if (continueAfterPageReload.isNotEmpty()) {
                    executeJS(continueAfterPageReload.last(), webView)
                    continueAfterPageReload.removeLast()
                    return
                }
                if (loginTrialsCounter == scripts.size) {
                    return
                }
                val script2 = scripts[loginTrialsCounter]
                executeJS(script2.call(), webView)
                loginTrialsCounter++
                Helper.showDeveloperNotification(context, "onPageFinished")
            }
        }, 1000L
        )

    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        Log.e("Error", "loading web view: request: $request error: $error")
    }

    //https://stackoverflow.com/questions/39749235/website-login-by-using-webview-javascript-android
    override fun onPageFinished(view: WebView, url: String) {
        if (loginTrialsCounter == 0) {
            // Initial call, next calls will happen to callback.
            executeJS(getJSLoginCode(), webView)
        }
    }

    private fun doSomeRandomActions (webView: WebView) {
        val randomActionCount = Random(98).nextInt(2, 11)
        (0..randomActionCount).forEach {
            val randomAction = Random(-123167).nextInt(0, 8)

            val randomTimeout = Random(1) .nextInt(581, 4129)
            val action = when (randomAction) {
                2 -> getJSSearchCode()
                4 -> getJSGetBadgeCallback()
                5 -> getJSOpenSidebarCode()
                else -> getJSSearchCode() + getJSOpenQuestionCode()
            }
            executeJS(action, webView, randomTimeout)
        }
    }

    private fun executeJS(jsCode: String, webView: WebView, timeout: Int = 0) {
        // Always overwrite executeFunction to clear old commands.
        val functionWrapper = """
            javascript: {
                async function sleep (duration) {
                    await new Promise(r => setTimeout(r, duration))
                }
                /* Async to allow sleep calls. */
                async function executeCode() {
                        $jsCode
                        window.$ANDROID_CALLBACK.commandFinished();
                 };
                 /* Own Jquery as we need async.. May lead to errors or conflicts.. */
                 var myScript = document.createElement('script');
                 myScript.src = 'http://code.jquery.com/jquery-1.9.1.min.js';
                 myScript.onload = function() {
                   console.log('jQuery loaded.');
                   setTimeout(executeCode, $timeout)
                 };
                 document.body.appendChild(myScript);
            };
        """.trimIndent()
        // Needed as we get called from PageFinished but as well from callback thread.
        webView.post {
            webView.loadUrl(functionWrapper)
        }
    }

    fun getJSLoginCode (): String {
        val user = PersistenceHelper.getUser(context)
        return  """
            console.log('getJSLoginCode');
            if (!$JS_EXPRESSION_SUCCESSFUL_LOGIN) {
                document.getElementById('email').value = '${user.userName}';
                document.getElementById('password').value = '${user.password}';
                document.getElementById('submit-button').click();
            }
        """.trimIndent()
    }

    fun getJSLoginAndroidCallback (): String {
        return """
            console.log('getJSLoginAndroidCallback');
            window.$ANDROID_CALLBACK.login($JS_EXPRESSION_SUCCESSFUL_LOGIN);
        """.trimIndent()

    }

    private fun getJSOpenQuestionCode (): String {
        return """
            console.log('getJSOpenQuestionCode');
            const scrollposition = Math.floor(Math.random() * 1000 + 567);
            scroll(0, scrollposition);
            const elements = '$('div#mainbar div.-summary div.-details h2 a');
            const index = Math.floor(Math.random() * elements.length);
            elements[index].click()
        """.trimIndent()
    }

    private fun getJSOpenSidebarCode (): String {
        return """
            console.log('getJSOpenSidebarCode');
            $('a.js-site-switcher-button').click()
        """.trimIndent()
    }
    
    private fun getJSSearchCode (): String {
        return """
           console.log('getJSSearchCode');
           const values = ['Android', 'Fragment lifecycle', 'Bugs', 'Swift', 'Log4J', 'Typescript', 'Mastertheorem', 'Java', 'Spring', 'Backdoors', 'docker', 'Helm', 'typos', 'generator']
           const randomValue = Math.floor(Math.random() * values.length);

           if ($('input.js-search-input').length > 0) {
             $('a.js-search-trigger').click();
             $('input.js-search-input')[0].value = randomValue;
             $('form.js-search-container').get(0).submit();
           }

           if ($('input.js-search-field').length > 0) {
             $('a.js-searchbar-trigger').click();
             $('input.js-search-field')[0].value = randomValue;
             $('form.js-searchbar').get(0).submit();
           }
        """.trimIndent()
    }

    fun getJSGetBadgeCallback (): String {
        val goToUserPage = """
            console.log('getJSGetBadgeCallback');
            $('a.js-site-switcher-button').click();
            $('a.s-user-card--link').get(0).click();
            """

        continueAfterPageReload.add("""
            $('button.js-select-badge').click();
            
            await sleep(1000); /* Await popup opened */
            const enthusiast = $('span:contains("Enthusiast")').get(1).innerHTML;
            window.$ANDROID_CALLBACK.badges(enthusiast);
            const fanatic = $('span:contains("Fanatic")').get(1).innerHTML;
            window.$ANDROID_CALLBACK.badges(fanatic);  
        """.trimIndent())

        return goToUserPage
    }

}