package ir.hanzodev1375.ghostide.translator.util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import ir.hanzodev1375.ghostide.translator.model.StringEntry;

public class StringsXmlParser {
  public static List<StringEntry> parse(File xmlFile) throws Exception {
    List<StringEntry> entries = new ArrayList<>();
    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    XmlPullParser parser = factory.newPullParser();
    parser.setInput(new FileInputStream(xmlFile), "UTF-8");
    int event = parser.getEventType();
    while (event != XmlPullParser.END_DOCUMENT) {
      if (event == XmlPullParser.START_TAG && "string".equals(parser.getName())) {
        String name = parser.getAttributeValue(null, "name");
        String translatableAttr = parser.getAttributeValue(null, "translatable");
        boolean translatable = !"false".equals(translatableAttr);
        String value = parser.nextText();
        if (name != null && !name.isEmpty()) {
          entries.add(new StringEntry(name, value, translatable));
        }
      }
      event = parser.next();
    }
    return entries;
  }

  public static void write(File outputFile, List<StringEntry> entries) throws Exception {
    outputFile.getParentFile().mkdirs();
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");
    for (StringEntry entry : entries) {
      String escaped = escapeXml(entry.value);
      sb.append("  <string name=\"")
          .append(entry.name)
          .append("\">")
          .append(escaped)
          .append("</string>\n");
    }
    sb.append("</resources>");
    try (FileWriter fw = new FileWriter(outputFile)) {
      fw.write(sb.toString());
    }
  }

  private static String escapeXml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }
}
