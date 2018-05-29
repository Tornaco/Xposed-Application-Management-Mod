package github.tornaco.xposedmoduletest;

interface ITaskRemoveListener {
    oneway void onTaskRemoved(String packageName);
}
