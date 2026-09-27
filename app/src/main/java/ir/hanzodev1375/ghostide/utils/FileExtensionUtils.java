package ir.hanzodev1375.ghostide.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class FileExtensionUtils {

    private FileExtensionUtils() {}

    private static final Set<String> CODE_EXTENSIONS;
    private static final Set<String> IMAGE_EXTENSIONS;
    private static final Set<String> AUDIO_EXTENSIONS;

    static {
        Map<String, Boolean> code = new HashMap<>();
        String[] langs = {
                ".html", ".htm", ".xhtml", ".shtml",
                ".css", ".scss", ".sass", ".less", ".styl", ".stylus",
                ".js", ".mjs", ".cjs", ".jsx",
                ".ts", ".mts", ".cts", ".tsx",
                ".vue", ".svelte", ".astro",
                ".ejs", ".hbs", ".handlebars", ".mustache",
                ".pug", ".jade", ".haml", ".slim",
                ".twig", ".liquid", ".njk", ".erb",
                ".cshtml", ".razor", ".aspx", ".ascx", ".jsp", ".jspx", ".gsp",
                ".java", ".kt", ".kts", ".groovy", ".gradle", ".scala", ".sc",
                ".clj", ".cljs", ".cljc", ".edn",
                ".cs", ".vb", ".fs", ".fsx", ".fsi",
                ".go", ".rs", ".swift", ".dart", ".nim", ".zig", ".v",
                ".php", ".phtml", ".php3", ".php4", ".php5", ".php7", ".phps",
                ".rb", ".erb", ".rake", ".gemspec", ".ru",
                ".py", ".pyw", ".pyi", ".pyx", ".pxd",
                ".pl", ".pm", ".t", ".pod",
                ".lua", ".luau",
                ".r", ".rmd", ".rnw",
                ".jl",
                ".ex", ".exs", ".eex", ".heex", ".leex",
                ".erl", ".hrl", ".escript",
                ".hs", ".lhs",
                ".ml", ".mli", ".mll", ".mly",
                ".elm",
                ".cr",
                ".hx", ".hxml",
                ".purs",
                ".raku", ".rakumod", ".rakutest", ".p6", ".pm6", ".pl6",
                ".tcl", ".awk", ".sed",
                ".pro", ".p",
                ".coffee",
                ".sol",
                ".move",
                ".cairo",
                ".c", ".h",
                ".cpp", ".cxx", ".cc", ".c++", ".hpp", ".hxx", ".hh", ".h++", ".inl", ".ipp", ".tpp",
                ".m", ".mm",
                ".cu", ".cuh",
                ".asm", ".s", ".nasm", ".a51", ".inc", ".lst",
                ".sh", ".bash", ".bashrc", ".bash_profile", ".bash_login", ".bash_logout",
                ".zsh", ".zshrc", ".zprofile", ".zlogin",
                ".ash", ".ksh", ".csh", ".tcsh", ".fish",
                ".ps1", ".psm1", ".psd1", ".ps1xml",
                ".bat", ".cmd", ".btm",
                ".smali", ".aidl",
                ".json", ".json5", ".jsonc", ".jsonl", ".geojson",
                ".xml", ".xsd", ".xsl", ".xslt", ".dtd", ".plist", ".xaml", ".svg",
                ".yaml", ".yml",
                ".toml",
                ".ini", ".cfg", ".conf", ".config", ".cnf", ".properties", ".env",
                ".editorconfig", ".gitignore", ".gitattributes", ".gitconfig",
                ".htaccess", ".htpasswd",
                ".md", ".markdown", ".mdx", ".mkd", ".rst", ".adoc", ".asciidoc", ".textile",
                ".tex", ".ltx", ".sty", ".cls", ".bib", ".bst",
                ".csv", ".tsv", ".dif",
                ".sql", ".psql", ".plsql", ".mysql", ".pgsql", ".tsql", ".hql", ".cql",
                ".graphql", ".gql", ".graphqls",
                ".proto", ".thrift", ".avsc", ".avdl", ".capnp", ".fbs",
                ".prisma", ".schema",
                ".cmake", ".ninja", ".mk", ".mak", ".make", ".makefile",
                ".meson", ".bazel", ".bzl", ".buck",
                ".sbt", ".podspec", ".podfile",
                ".dockerfile", ".containerfile",
                ".tf", ".tfvars", ".hcl", ".nomad", ".bicep",
                ".gradle.kts",
                ".sln", ".csproj", ".vbproj", ".fsproj", ".vcxproj", ".vcproj", ".xcodeproj",
                ".nuspec", ".nsi", ".nsh",
                ".spec", ".rules",
                ".ipynb", ".nb",
                ".vhd", ".vhdl", ".sv", ".svh", ".vh",
                ".ucf", ".xdc", ".sdc", ".qsf", ".qpf",
                ".g4", ".thy", ".dfy",
                ".zig.zon", ".workflow", ".pipeline",
                ".lock", ".sum"
        };
        for (String ext : langs) code.put(ext.toLowerCase(), Boolean.TRUE);
        CODE_EXTENSIONS = Collections.unmodifiableSet(code.keySet());

        IMAGE_EXTENSIONS = Set.of(
                ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".tiff", ".tif",
                ".avif", ".webp", ".svg", ".svgz", ".ico", ".heic", ".heif",
                ".psd", ".ai", ".eps", ".raw", ".cr2", ".nef", ".dng"
        );

        AUDIO_EXTENSIONS = Set.of(
                ".mp3", ".wav", ".m4a", ".ogg", ".oga", ".flac", ".aac",
                ".opus", ".wma", ".aiff", ".aif", ".alac", ".amr", ".ac3",
                ".mid", ".midi", ".ape", ".wv"
        );
    }

    public static boolean isCodeFile(String extension) {
        return extension != null && CODE_EXTENSIONS.contains(extension.toLowerCase());
    }

    public static boolean isImageFile(String extension) {
        return extension != null && IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    public static boolean isAudioFile(String extension) {
        return extension != null && AUDIO_EXTENSIONS.contains(extension.toLowerCase());
    }

    public static String getExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf(".");
        return (lastDot > 0) ? fileName.substring(lastDot).toLowerCase() : "";
    }

    public static Set<String> getCodeExtensions() {
        return CODE_EXTENSIONS;
    }

    public static Set<String> getImageExtensions() {
        return IMAGE_EXTENSIONS;
    }

    public static Set<String> getAudioExtensions() {
        return AUDIO_EXTENSIONS;
    }
}