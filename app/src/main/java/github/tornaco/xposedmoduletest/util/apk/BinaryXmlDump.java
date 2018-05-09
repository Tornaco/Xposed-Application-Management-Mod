package github.tornaco.xposedmoduletest.util.apk;


import github.tornaco.xposedmoduletest.util.apk.xml.Attribute;
import github.tornaco.xposedmoduletest.util.apk.xml.BinaryXmlListener;

public class BinaryXmlDump implements BinaryXmlListener {

    public void onXmlEntry(String path, String name, Attribute... attrs) {
        System.out.printf("%s -> <%s%n", path, name);
        String indent = path.replaceAll(".", " ");
        for (Attribute attr : attrs) {
            System.out.printf("%s      %s=\"%s\"%n", indent, attr.getName(), attr.getValue());
        }
    }
}
