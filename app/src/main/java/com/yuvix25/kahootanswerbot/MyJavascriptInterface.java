package com.yuvix25.kahootanswerbot;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;


class MyJavascriptInterface {
    private final MainActivity mainActivity;
    private final WebView webView;

    MyJavascriptInterface(MainActivity mainActivity, WebView webView) {
        this.mainActivity = mainActivity;
        this.webView = webView;
    }

    @JavascriptInterface
    public String searchKahoot(String query, String callback) {
        String url = "https://create.kahoot.it/rest/kahoots/?query=" + query + "&limit=100";
        Thread thread = new Thread(() -> {
            try {
                String response = Utils.httpRequest(url);
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            webView.evaluateJavascript("(" + callback + ")(" + new JSONObject(response).toString() + ")", null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return null;

    }

    @JavascriptInterface
    public String getAnswers(String uuid) {
        String url = "https://create.kahoot.it/rest/kahoots/" + uuid + "/card/?includeKahoot=true";
        try {
            String res = Utils.httpRequest(url);
            JSONArray questions = new JSONObject(res).getJSONObject("kahoot").getJSONArray("questions");
            int[] answers = new int[questions.length()];
            for (int i = 0; i < questions.length(); i++) {
                for (int j = 0; j < questions.getJSONObject(i).getJSONArray("choices").length(); j++) {
                    if (questions.getJSONObject(i).getJSONArray("choices").getJSONObject(j).getBoolean("correct")) {
                        answers[i] = j;
                        break;
                    }
                }
            }
            return new JSONArray(answers).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
