package github.tornaco.xposedmoduletest;

interface IJsEvaluateListener {
   oneway void onFinish(String res);
   oneway void onError(String message, String trace);
}
