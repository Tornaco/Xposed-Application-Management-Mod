package github.tornaco.xposedmoduletest.util.apk.xml;


public interface BinaryXmlListener {
    void onXmlEntry(String path, String name, Attribute... attrs);
}