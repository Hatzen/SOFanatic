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

class WebViewLoginHandler(val context: Context): WebViewClient() {
    companion object {
        val ANDROID_CALLBACK = "ANDROID_CALLBACK"
    }

    private val JS_EXPRESSION_SUCCESSFUL_LOGIN = "document.getElementsByClassName('js-inbox-button').length > 0"
    private var loginTrialsCounter = 0
    val scripts =
        listOf(
            getJSLoginCode(),
            getJSLoginAndroidCallback(),
            getJSGetBadgeCallback()
        )

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        Log.e("Error", "loading web view: request: $request error: $error")
    }

    //https://stackoverflow.com/questions/39749235/website-login-by-using-webview-javascript-android
    override fun onPageFinished(view: WebView, url: String) {
        // TODO: Some actions call a side change, which will interrupt script execution and may lead to cycles..
        //  Best bet is to check URL and store every code in functions executing each other?

        // TODO: Do we need to reset this somehow? Otherwise it depends on reinitalization of service or activity
         // if (loginTrialsCounter < 3) {
        executeJS(scripts[loginTrialsCounter % scripts.size], view, 2000)
            // doSomeRandomActions(view)
            Helper.showNotification(context, "onPageFinished")
        // }
        loginTrialsCounter++
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
        val functionWrapper = """
            javascript: {
                function sleep (duration) {
                    await Promise.resolve(r => setTimeout(r, duration))
                }
                async function executeCode() {
                        $jsCode
                 };
                 setTimeout(executeCode, $timeout)
            };
        """.trimIndent()

        // Always overwrite executeFunction to clear old commands.
        webView.loadUrl(
            "javascript: {" +
                    "function executeCode() {" +
                        jsCode +
                    "};" +
                    "setTimeout(executeCode, " + timeout + ")" +
                "};"
        )
    }

    private fun getJSLoginCode (): String {
        val user = PersistenceHelper.getUser(context)
        return  """
            document.getElementById('email').value = '${user.userName}';
            "document.getElementById('password').value = '${user.password}';
            "document.getElementById('submit-button').click();"
        """.trimIndent()
    }

    private fun getJSLoginAndroidCallback (): String {
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

    private fun getJSGetBadgeCallback (): String {
        return """
            console.log('getJSGetBadgeCallback');
            $('a.js-site-switcher-button').click();
            $('a.s-user-card--link').get(0).click();
            
            // Here the page gets loaded..
            
            $('button.js-select-badge').click();
            
            sleep(2000)
            const enthusiast = $('span:contains("Enthusiast")').get(1).innerHTML;
            window.$ANDROID_CALLBACK.badges(enthusiast);
            const fanatic = $('span:contains("Fanatic")').get(1).innerHTML;
            window.$ANDROID_CALLBACK.badges(fanatic);  
        """.trimIndent()
    }

}


/*
div#mainbar div.-summary div.-details h2 a

// Open question.
const scrollposition = Math.floor(Math.random() * 1000 + 567);
scroll(0, scrollposition);
const elements = $('div#mainbar div.-summary div.-details h2 a');
const index = Math.floor(Math.random() * elements.length);
elements[index].click()

// Open Sidebar
$('a.js-site-switcher-button').click()



// Search
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


// Profile
$('a.my-profile').click()
$('button.js-select-badge').click()
$('span:contains("Enthusiast")').get(1).innerHTML
$('span:contains("Fanatic")').get(1).innerHTML

 */