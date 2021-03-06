package com.intel.cedar.engine.xml;

import java.util.HashMap;

/**
 * Well-known names used in XSLT processing. These names must all have
 * fingerprints in the range 0-1023, to avoid clashing with codes allocated in a
 * NamePool. We use the top three bits for the namespace, and the bottom seven
 * bits for the local name.
 * <p/>
 * <p>
 * Codes in the range 0-100 are used for standard node kinds such as ELEMENT,
 * DOCUMENT, etc, and for built-in types such as ITEM and NEEDVISITDATABASE.
 * </p>
 */

public abstract class StandardNames {

    private static final int DFLT_NS = 0;
    private static final int XSL_NS = 1;
    private static final int XML_NS = 2;
    private static final int XS_NS = 3;
    private static final int XSI_NS = 4;
    private static final int CEDAR_NS = 5;

    public static final int DFLT = 0; // 0
    public static final int XSL = 128; // 128
    public static final int XML = 128 * 2; // 256
    public static final int XS = 128 * 3; // 384
    public static final int XSI = 128 * 4; // 512
    public static final int CEDAR = 128 * 5; // 640

    public static final int XSL_ANALYZE_STRING = XSL;
    public static final int XSL_APPLY_IMPORTS = XSL + 1;
    public static final int XSL_APPLY_TEMPLATES = XSL + 2;
    public static final int XSL_ATTRIBUTE = XSL + 3;
    public static final int XSL_ATTRIBUTE_SET = XSL + 4;
    public static final int XSL_CALL_TEMPLATE = XSL + 5;
    public static final int XSL_CHARACTER_MAP = XSL + 6;
    public static final int XSL_CHOOSE = XSL + 7;
    public static final int XSL_COMMENT = XSL + 10;
    public static final int XSL_COPY = XSL + 11;
    public static final int XSL_COPY_OF = XSL + 12;
    public static final int XSL_DECIMAL_FORMAT = XSL + 13;
    public static final int XSL_DOCUMENT = XSL + 14;
    public static final int XSL_ELEMENT = XSL + 15;
    public static final int XSL_FALLBACK = XSL + 16;
    public static final int XSL_FOR_EACH = XSL + 17;
    public static final int XSL_FOR_EACH_GROUP = XSL + 20;
    public static final int XSL_FUNCTION = XSL + 21;
    public static final int XSL_IF = XSL + 22;
    public static final int XSL_IMPORT = XSL + 23;
    public static final int XSL_IMPORT_SCHEMA = XSL + 24;
    public static final int XSL_INCLUDE = XSL + 25;
    public static final int XSL_KEY = XSL + 26;
    public static final int XSL_MATCHING_SUBSTRING = XSL + 27;
    public static final int XSL_MESSAGE = XSL + 30;
    public static final int XSL_NEXT_MATCH = XSL + 31;
    public static final int XSL_NUMBER = XSL + 32;
    public static final int XSL_NAMESPACE = XSL + 33;
    public static final int XSL_NAMESPACE_ALIAS = XSL + 34;
    public static final int XSL_NON_MATCHING_SUBSTRING = XSL + 35;
    public static final int XSL_OTHERWISE = XSL + 36;
    public static final int XSL_OUTPUT = XSL + 37;
    public static final int XSL_OUTPUT_CHARACTER = XSL + 41;
    public static final int XSL_PARAM = XSL + 42;
    public static final int XSL_PERFORM_SORT = XSL + 43;
    public static final int XSL_PRESERVE_SPACE = XSL + 44;
    public static final int XSL_PROCESSING_INSTRUCTION = XSL + 45;
    public static final int XSL_RESULT_DOCUMENT = XSL + 46;
    public static final int XSL_SEQUENCE = XSL + 47;
    public static final int XSL_SORT = XSL + 50;
    public static final int XSL_STRIP_SPACE = XSL + 51;
    public static final int XSL_STYLESHEET = XSL + 52;
    public static final int XSL_TEMPLATE = XSL + 53;
    public static final int XSL_TEXT = XSL + 54;
    public static final int XSL_TRANSFORM = XSL + 55;
    public static final int XSL_VALUE_OF = XSL + 56;
    public static final int XSL_VARIABLE = XSL + 57;
    public static final int XSL_WITH_PARAM = XSL + 60;
    public static final int XSL_WHEN = XSL + 61;

    public static final int XSL_DEFAULT_COLLATION = XSL + 100;
    public static final int XSL_EXCLUDE_RESULT_PREFIXES = XSL + 101;
    public static final int XSL_EXTENSION_ELEMENT_PREFIXES = XSL + 102;
    public static final int XSL_INHERIT_NAMESPACES = XSL + 103;
    public static final int XSL_TYPE = XSL + 104;
    public static final int XSL_USE_ATTRIBUTE_SETS = XSL + 105;
    public static final int XSL_USE_WHEN = XSL + 106;
    public static final int XSL_VALIDATION = XSL + 107;
    public static final int XSL_VERSION = XSL + 108;
    public static final int XSL_XPATH_DEFAULT_NAMESPACE = XSL + 109;

    public static final int XML_BASE = XML + 1;
    public static final int XML_SPACE = XML + 2;
    public static final int XML_LANG = XML + 3;
    public static final int XML_ID = XML + 4;

    public static final String ALPHANUMERIC = "alphanumeric";
    public static final String ARCHIVE = "archive";
    public static final String AS = "as";
    public static final String BYTE_ORDER_MARK = "byte-order-mark";
    public static final String CASE_ORDER = "case-order";
    public static final String CDATA_SECTION_ELEMENTS = "cdata-section-elements";
    public static final String CHARACTER = "character";
    public static final String CLASS = "class";
    public static final String COLLATION = "collation";
    public static final String COPY_NAMESPACES = "copy-namespaces";
    public static final String COUNT = "count";
    public static final String DATA_TYPE = "data-type";
    public static final String DECIMAL_SEPARATOR = "decimal-separator";
    public static final String DECOMPOSITION = "decomposition";
    public static final String DEFAULT = "default";
    public static final String DEFAULT_COLLATION = "default-collation";
    public static final String DEFAULT_VALIDATION = "default-validation";
    public static final String DIGIT = "digit";
    public static final String DISABLE_OUTPUT_ESCAPING = "disable-output-escaping";
    public static final String DOCTYPE_PUBLIC = "doctype-public";
    public static final String DOCTYPE_SYSTEM = "doctype-system";
    public static final String ELEMENTS = "elements";
    public static final String ESCAPE_URI_ATTRIBUTES = "escape-uri-attributes";
    public static final String ENCODING = "encoding";
    public static final String EXCLUDE_RESULT_PREFIXES = "exclude-result-prefixes";
    public static final String EXTENSION_ELEMENT_PREFIXES = "extension-element-prefixes";
    public static final String FLAGS = "flags";
    public static final String FORMAT = "format";
    public static final String FROM = "from";
    public static final String GROUP_ADJACENT = "group-adjacent";
    public static final String GROUP_BY = "group-by";
    public static final String GROUP_ENDING_WITH = "group-ending-with";
    public static final String GROUP_STARTING_WITH = "group-starting-with";
    public static final String GROUPING_SEPARATOR = "grouping-separator";
    public static final String GROUPING_SIZE = "grouping-size";
    public static final String HREF = "href";
    public static final String ID = "id";
    public static final String IGNORE_CASE = "ignore-case";
    public static final String IGNORE_MODIFIERS = "ignore-modifiers";
    public static final String IGNORE_SYMBOLS = "ignore-symbols";
    public static final String IGNORE_WIDTH = "ignore-width";
    public static final String IMPLEMENTS_PREFIX = "implements-prefix";
    public static final String INCLUDE_CONTENT_TYPE = "include-content-type";
    public static final String INDENT = "indent";
    public static final String INFINITY = "infinity";
    public static final String INHERIT_NAMESPACES = "inherit-namespaces";
    public static final String INPUT_TYPE_ANNOTATIONS = "input-type-annotations";
    public static final String LANG = "lang";
    public static final String LANGUAGE = "language";
    public static final String LETTER_VALUE = "letter-value";
    public static final String LEVEL = "level";
    public static final String MATCH = "match";
    public static final String MEDIA_TYPE = "media-type";
    public static final String METHOD = "method";
    public static final String MINUS_SIGN = "minus-sign";
    public static final String MODE = "mode";
    public static final String NAME = "name";
    public static final String NAMESPACE = "namespace";
    public static final String NAN = "NaN";
    public static final String NORMALIZATION_FORM = "normalization-form";
    public static final String OMIT_XML_DECLARATION = "omit-xml-declaration";
    public static final String ORDER = "order";
    public static final String ORDINAL = "ordinal";
    public static final String OUTPUT_VERSION = "output-version";
    public static final String OVERRIDE = "override";
    public static final String PATTERN_SEPARATOR = "pattern-separator";
    public static final String PERCENT = "percent";
    public static final String PER_MILLE = "per-mille";
    public static final String PRIORITY = "priority";
    public static final String REGEX = "regex";
    public static final String REQUIRED = "required";
    public static final String RESULT_PREFIX = "result-prefix";
    public static final String RULES = "rules";
    public static final String SCHEMA_LOCATION = "schema-location";
    public static final String SELECT = "select";
    public static final String SEPARATOR = "separator";
    public static final String SRC = "src";
    public static final String STABLE = "stable";
    public static final String STANDALONE = "standalone";
    public static final String STRENGTH = "strength";
    public static final String STRING = "string";
    public static final String STYLESHEET_PREFIX = "stylesheet-prefix";
    public static final String TERMINATE = "terminate";
    public static final String TEST = "test";
    public static final String TUNNEL = "tunnel";
    public static final String TYPE = "type";
    public static final String UNDECLARE_PREFIXES = "undeclare-prefixes";
    public static final String USE = "use";
    public static final String USE_ATTRIBUTE_SETS = "use-attribute-sets";
    public static final String USE_CHARACTER_MAPS = "use-character-maps";
    public static final String USE_WHEN = "use-when";
    public static final String VALIDATION = "validation";
    public static final String VALUE = "value";
    public static final String VERSION = "version";
    public static final String XPATH_DEFAULT_NAMESPACE = "xpath-default-namespace";
    public static final String ZERO_DIGIT = "zero-digit";

    public static final int XS_STRING = XS + 1;
    public static final int XS_BOOLEAN = XS + 2;
    public static final int XS_DECIMAL = XS + 3;
    public static final int XS_FLOAT = XS + 4;
    public static final int XS_DOUBLE = XS + 5;
    public static final int XS_DURATION = XS + 6;
    public static final int XS_DATE_TIME = XS + 7;
    public static final int XS_TIME = XS + 8;
    public static final int XS_DATE = XS + 9;
    public static final int XS_G_YEAR_MONTH = XS + 10;
    public static final int XS_G_YEAR = XS + 11;
    public static final int XS_G_MONTH_DAY = XS + 12;
    public static final int XS_G_DAY = XS + 13;
    public static final int XS_G_MONTH = XS + 14;
    public static final int XS_HEX_BINARY = XS + 15;
    public static final int XS_BASE64_BINARY = XS + 16;
    public static final int XS_ANY_URI = XS + 17;
    public static final int XS_QNAME = XS + 18;
    public static final int XS_NOTATION = XS + 19;
    public static final int XS_INTEGER = XS + 20;

    // Note that any type code <= XS_INTEGER is considered to represent a
    // primitive type: see Type.isPrimitiveType()

    public static final int XS_NON_POSITIVE_INTEGER = XS + 21;
    public static final int XS_NEGATIVE_INTEGER = XS + 22;
    public static final int XS_LONG = XS + 23;
    public static final int XS_INT = XS + 24;
    public static final int XS_SHORT = XS + 25;
    public static final int XS_BYTE = XS + 26;
    public static final int XS_NON_NEGATIVE_INTEGER = XS + 27;
    public static final int XS_POSITIVE_INTEGER = XS + 28;
    public static final int XS_UNSIGNED_LONG = XS + 29;
    public static final int XS_UNSIGNED_INT = XS + 30;
    public static final int XS_UNSIGNED_SHORT = XS + 31;
    public static final int XS_UNSIGNED_BYTE = XS + 32;
    public static final int XS_NORMALIZED_STRING = XS + 41;
    public static final int XS_TOKEN = XS + 42;
    public static final int XS_LANGUAGE = XS + 43;
    public static final int XS_NMTOKEN = XS + 44;
    public static final int XS_NMTOKENS = XS + 45; // NB: list type
    public static final int XS_NAME = XS + 46;
    public static final int XS_NCNAME = XS + 47;
    public static final int XS_ID = XS + 48;
    public static final int XS_IDREF = XS + 49;
    public static final int XS_IDREFS = XS + 50; // NB: list type
    public static final int XS_ENTITY = XS + 51;
    public static final int XS_ENTITIES = XS + 52; // NB: list type

    public static final int XS_ANY_TYPE = XS + 60;
    public static final int XS_ANY_SIMPLE_TYPE = XS + 61;

    public static final int XS_INVALID_NAME = XS + 62;

    public static final int XS_ALL = XS + 70;
    public static final int XS_ANNOTATION = XS + 71;
    public static final int XS_ANY = XS + 72;
    public static final int XS_ANY_ATTRIBUTE = XS + 73;
    public static final int XS_APPINFO = XS + 74;
    public static final int XS_ATTRIBUTE = XS + 75;
    public static final int XS_ATTRIBUTE_GROUP = XS + 76;
    public static final int XS_CHOICE = XS + 77;
    public static final int XS_COMPLEX_CONTENT = XS + 80;
    public static final int XS_COMPLEX_TYPE = XS + 81;
    public static final int XS_DOCUMENTATION = XS + 82;
    public static final int XS_ELEMENT = XS + 83;
    public static final int XS_ENUMERATION = XS + 84;
    public static final int XS_EXTENSION = XS + 85;
    public static final int XS_FIELD = XS + 86;
    public static final int XS_FRACTION_DIGITS = XS + 87;
    public static final int XS_GROUP = XS + 88;
    public static final int XS_IMPORT = XS + 90;
    public static final int XS_INCLUDE = XS + 91;
    public static final int XS_KEY = XS + 92;
    public static final int XS_KEYREF = XS + 93;
    public static final int XS_LENGTH = XS + 94;
    public static final int XS_LIST = XS + 95;
    public static final int XS_MAX_EXCLUSIVE = XS + 96;
    public static final int XS_MAX_INCLUSIVE = XS + 97;
    public static final int XS_MAX_LENGTH = XS + 100;
    public static final int XS_MIN_EXCLUSIVE = XS + 101;
    public static final int XS_MIN_INCLUSIVE = XS + 102;
    public static final int XS_MIN_LENGTH = XS + 103;
    public static final int XS_notation = XS + 104;
    public static final int XS_PATTERN = XS + 105;
    public static final int XS_REDEFINE = XS + 106;
    public static final int XS_RESTRICTION = XS + 107;
    public static final int XS_SCHEMA = XS + 108;
    public static final int XS_SELECTOR = XS + 110;
    public static final int XS_SEQUENCE = XS + 111;
    public static final int XS_SIMPLE_CONTENT = XS + 112;
    public static final int XS_SIMPLE_TYPE = XS + 113;
    public static final int XS_TOTAL_DIGITS = XS + 114;
    public static final int XS_UNION = XS + 115;
    public static final int XS_UNIQUE = XS + 116;
    public static final int XS_WHITE_SPACE = XS + 117;

    public static final int XS_UNTYPED = XS + 118;
    public static final int XS_UNTYPED_ATOMIC = XS + 119;
    public static final int XS_ANY_ATOMIC_TYPE = XS + 120;
    public static final int XS_YEAR_MONTH_DURATION = XS + 121;
    public static final int XS_DAY_TIME_DURATION = XS + 122;
    public static final int XS_NUMERIC = XS + 123;

    public static final int XS_ASSERT = XS + 124;
    // public static final int XS_REPORT = XS + 125;

    public static final int XSI_TYPE = XSI + 1;
    public static final int XSI_NIL = XSI + 2;
    public static final int XSI_SCHEMA_LOCATION = XSI + 3;
    public static final int XSI_NO_NAMESPACE_SCHEMA_LOCATION = XSI + 4;
    public static final int XSI_SCHEMA_LOCATION_TYPE = XSI + 5;

    // cedar
    public static final int CEDAR_FEATURE = CEDAR + 1;
    public static final int CEDAR_IMPORT = CEDAR + 2;
    public static final int CEDAR_DECLARATION = CEDAR + 3;
    public static final int CEDAR_FEATUREUI = CEDAR + 4;
    public static final int CEDAR_FLOW = CEDAR + 5;
    public static final int CEDAR_NAME = CEDAR + 6;
    public static final int CEDAR_VERSION = CEDAR + 7;
    public static final int CEDAR_VARIABLE = CEDAR + 8;
    public static final int CEDAR_ID = CEDAR + 9;
    public static final int CEDAR_TITLE = CEDAR + 10;
    public static final int CEDAR_WINDOW = CEDAR + 11;
    public static final int CEDAR_FORM = CEDAR + 12;
    public static final int CEDAR_FIELDSET = CEDAR + 13;
    public static final int CEDAR_LABEL = CEDAR + 14;
    public static final int CEDAR_COMBO = CEDAR + 15;
    public static final int CEDAR_VALUE = CEDAR + 16;
    public static final int CEDAR_CHECKBOXGROUP = CEDAR + 17;
    public static final int CEDAR_LISTFIELD = CEDAR + 18;
    public static final int CEDAR_COMPOSITE = CEDAR + 19;
    public static final int CEDAR_COMBOITEM = CEDAR + 20;
    public static final int CEDAR_SELECT = CEDAR + 21;
    public static final int CEDAR_SEQUENCE = CEDAR + 22;
    public static final int CEDAR_TASKLET = CEDAR + 23;
    public static final int CEDAR_MACHINE = CEDAR + 24;
    public static final int CEDAR_PROPERTIES = CEDAR + 25;
    public static final int CEDAR_PROPERTY = CEDAR + 26;
    public static final int CEDAR_COUNT = CEDAR + 27;
    public static final int CEDAR_CPU = CEDAR + 28;
    public static final int CEDAR_MEM = CEDAR + 29;
    public static final int CEDAR_DISK = CEDAR + 30;
    public static final int CEDAR_CASE = CEDAR + 31;
    public static final int CEDAR_CASES = CEDAR + 32;
    public static final int CEDAR_PROVIDER = CEDAR + 33;
    public static final int CEDAR_PUBLIC = CEDAR + 34;
    public static final int CEDAR_VARIABLES = CEDAR + 35;
    public static final int CEDAR_TASKLETS = CEDAR + 36;
    public static final int CEDAR_VALUES = CEDAR + 37;
    public static final int CEDAR_CONTRIBUTER = CEDAR + 38;
    public static final int CEDAR_PARALLEL = CEDAR + 39;
    public static final int CEDAR_MIN = CEDAR + 40;
    public static final int CEDAR_MAX = CEDAR + 41;
    public static final int CEDAR_BIND = CEDAR + 42;
    public static final int CEDAR_XMLNS = CEDAR + 43;
    public static final int CEDAR_TEXTFIELD = CEDAR + 44;
    public static final int CEDAR_TEXTAREA = CEDAR + 45;
    public static final int CEDAR_SHOWONSELECT = CEDAR + 46;
    public static final int CEDAR_DEPENDS = CEDAR + 47;
    public static final int CEDAR_DEFAULT = CEDAR + 48;
    public static final int CEDAR_FILEUPLOADFIELD = CEDAR + 49;
    public static final int CEDAR_SVN = CEDAR + 50;
    public static final int CEDAR_URL = CEDAR + 51;
    public static final int CEDAR_REV = CEDAR + 52;
    public static final int CEDAR_LOG = CEDAR + 53;
    public static final int CEDAR_DEPEND = CEDAR + 54;
    public static final int CEDAR_REFID = CEDAR + 55;
    public static final int CEDAR_ACTION = CEDAR + 56;
    public static final int CEDAR_TESTCASES = CEDAR + 57;
    public static final int CEDAR_TESTCASE = CEDAR + 58;
    public static final int CEDAR_KEYS = CEDAR + 59;
    public static final int CEDAR_KEY = CEDAR + 60;
    public static final int CEDAR_ITEMS = CEDAR + 61;
    public static final int CEDAR_ITEM = CEDAR + 62;
    public static final int CEDAR_GLOBAL = CEDAR + 63;
    public static final int CEDAR_TOOL = CEDAR + 64;
    public static final int CEDAR_CASEPARSER = CEDAR + 65;
    public static final int CEDAR_LEVEL = CEDAR + 66;
    public static final int CEDAR_PARAMS = CEDAR + 67;
    public static final int CEDAR_PARAM = CEDAR + 68;
    public static final int CEDAR_TIMEOUT = CEDAR + 69;
    public static final int CEDAR_SUBMIT = CEDAR + 70;
    public static final int CEDAR_FORMITEM = CEDAR + 71;
    public static final int CEDAR_PARSER = CEDAR + 72;
    public static final int CEDAR_OS = CEDAR + 73;
    public static final int CEDAR_ARCH = CEDAR + 74;
    public static final int CEDAR_HOST = CEDAR + 75;
    public static final int CEDAR_SHARABLE = CEDAR + 76;
    public static final int CEDAR_ONFAIL = CEDAR + 77;
    public static final int CEDAR_LAUNCHES = CEDAR + 78;
    public static final int CEDAR_LAUNCHSET = CEDAR + 79;
    public static final int CEDAR_LAUNCH = CEDAR + 80;
    public static final int CEDAR_OPTION = CEDAR + 81;
    public static final int CEDAR_TRIGGERS = CEDAR + 82;
    public static final int CEDAR_TRIGGER = CEDAR + 83;
    public static final int CEDAR_SHORTCUTS = CEDAR + 84;
    public static final int CEDAR_SHORTCUT = CEDAR + 85;
    public static final int CEDAR_DEBUG = CEDAR + 86;
    public static final int CEDAR_RECYCLE = CEDAR + 87;
    public static final int CEDAR_INTERVAL = CEDAR + 88;
    public static final int CEDAR_GIT = CEDAR + 89;
    public static final int CEDAR_BRANCH = CEDAR + 90;
    public static final int CEDAR_VISIBLE = CEDAR + 91;
    
    public static final String STR_CEDAR_FEATURE = "feature";
    public static final String STR_CEDAR_IMPORT = "import";
    public static final String STR_CEDAR_DECLARATION = "desc";
    public static final String STR_CEDAR_FEATUREUI = "ui";
    public static final String STR_CEDAR_FLOW = "flow";
    public static final String STR_CEDAR_NAME = "name";
    public static final String STR_CEDAR_VERSION = "version";
    public static final String STR_CEDAR_VARIABLE = "variable";
    public static final String STR_CEDAR_ID = "id";
    public static final String STR_CEDAR_TITLE = "title";
    public static final String STR_CEDAR_WINDOW = "window";
    public static final String STR_CEDAR_FORM = "form";
    public static final String STR_CEDAR_FIELDSET = "fieldset";
    public static final String STR_CEDAR_LABEL = "label";
    public static final String STR_CEDAR_COMBO = "combo";
    public static final String STR_CEDAR_VALUE = "value";
    public static final String STR_CEDAR_CHECKBOXGROUP = "checkboxgroup";
    public static final String STR_CEDAR_LISTFIELD = "listfield";
    public static final String STR_CEDAR_COMPOSITE = "composite";
    public static final String STR_CEDAR_COMBOITEM = "comboitem";
    public static final String STR_CEDAR_SELECT = "select";
    public static final String STR_CEDAR_SEQUENCE = "sequence";
    public static final String STR_CEDAR_TASKLET = "tasklet";
    public static final String STR_CEDAR_MACHINE = "machine";
    public static final String STR_CEDAR_PROPERTIES = "properties";
    public static final String STR_CEDAR_PROPERTY = "property";
    public static final String STR_CEDAR_COUNT = "count";
    public static final String STR_CEDAR_CPU = "cpu";
    public static final String STR_CEDAR_MEM = "mem";
    public static final String STR_CEDAR_DISK = "disk";
    public static final String STR_CEDAR_CASE = "case";
    public static final String STR_CEDAR_CASES = "cases";
    public static final String STR_CEDAR_PROVIDER = "provider";
    public static final String STR_CEDAR_PUBLIC = "public";
    public static final String STR_CEDAR_VARIABLES = "variables";
    public static final String STR_CEDAR_TASKLETS = "tasklets";
    public static final String STR_CEDAR_VALUES = "values";
    public static final String STR_CEDAR_CONTRIBUTER = "contributer";
    public static final String STR_CEDAR_PARALLEL = "parallel";
    public static final String STR_CEDAR_MIN = "min";
    public static final String STR_CEDAR_MAX = "max";
    public static final String STR_CEDAR_BIND = "bind";
    public static final String STR_CEDAR_XMLNS = "xmlns";
    public static final String STR_CEDAR_TEXTFIELD = "textfield";
    public static final String STR_CEDAR_TEXTAREA = "textarea";
    public static final String STR_CEDAR_SHOWONSELECT = "show-on-select";
    public static final String STR_CEDAR_DEPENDS = "depends";
    public static final String STR_CEDAR_DEFAULT = "default";
    public static final String STR_CEDAR_FILEUPLOADFIELD = "fileuploadfield";
    public static final String STR_CEDAR_SVN = "svn";
    public static final String STR_CEDAR_URL = "url";
    public static final String STR_CEDAR_REV = "rev";
    public static final String STR_CEDAR_LOG = "log";
    public static final String STR_CEDAR_DEPEND = "depend";
    public static final String STR_CEDAR_REFID = "refid";
    public static final String STR_CEDAR_ACTION = "action";
    public static final String STR_CEDAR_TESTCASES = "testcases";
    public static final String STR_CEDAR_TESTCASE = "testcase";
    public static final String STR_CEDAR_KEYS = "keys";
    public static final String STR_CEDAR_KEY = "key";
    public static final String STR_CEDAR_ITEMS = "items";
    public static final String STR_CEDAR_ITEM = "item";
    public static final String STR_CEDAR_GLOBAL = "global";
    public static final String STR_CEDAR_TOOL = "tool";
    public static final String STR_CEDAR_CASEPARSER = "caseParser";
    public static final String STR_CEDAR_LEVEL = "level";
    public static final String STR_CEDAR_PARAMS = "params";
    public static final String STR_CEDAR_PARAM = "param";
    public static final String STR_CEDAR_TIMEOUT = "timeout";
    public static final String STR_CEDAR_SUBMIT = "submit";
    public static final String STR_CEDAR_FORMITEM = "formItem";
    public static final String STR_CEDAR_PARSER = "parser";
    public static final String STR_CEDAR_OS = "os";
    public static final String STR_CEDAR_ARCH = "arch";
    public static final String STR_CEDAR_HOST = "host";
    public static final String STR_CEDAR_SHARABLE = "sharable";
    public static final String STR_CEDAR_ONFAIL = "onfail";
    public static final String STR_CEDAR_LAUNCHES = "launches";
    public static final String STR_CEDAR_LAUNCHSET = "launchset";
    public static final String STR_CEDAR_LAUNCH = "launch";
    public static final String STR_CEDAR_OPTION = "option";
    public static final String STR_CEDAR_TRIGGERS = "triggers";
    public static final String STR_CEDAR_TRIGGER = "trigger";
    public static final String STR_CEDAR_SHORTCUTS = "shortcuts";
    public static final String STR_CEDAR_SHORTCUT = "shortcut";
    public static final String STR_CEDAR_DEBUG = "debug";
    public static final String STR_CEDAR_RECYCLE = "recycle";
    public static final String STR_CEDAR_INTERVAL = "interval";
    public static final String STR_CEDAR_GIT = "git";
    public static final String STR_CEDAR_BRANCH = "branch";
    public static final String STR_CEDAR_VISIBLE = "visible";
    
    private static String[] localNames = new String[1023];
    private static HashMap lookup = new HashMap(1023);

    // key is an expanded QName in Clark notation
    // value is a fingerprint, as a java.lang.Integer

    private StandardNames() {
    }

    private static void bindXSLTName(int constant, String localName) {
        localNames[constant] = localName;
        lookup.put('{' + NamespaceConstant.XSLT + '}' + localName, new Integer(
                constant));
    }

    private static void bindXMLName(int constant, String localName) {
        localNames[constant] = localName;
        lookup.put('{' + NamespaceConstant.XML + '}' + localName, new Integer(
                constant));
    }

    private static void bindXSName(int constant, String localName) {
        localNames[constant] = localName;
        lookup.put('{' + NamespaceConstant.SCHEMA + '}' + localName,
                new Integer(constant));
    }

    private static void bindXSIName(int constant, String localName) {
        localNames[constant] = localName;
        lookup.put('{' + NamespaceConstant.SCHEMA_INSTANCE + '}' + localName,
                new Integer(constant));
    }

    private static void bindCEDARName(int constant, String localName) {
        localNames[constant] = localName;
        lookup.put('{' + NamespaceConstant.CEDAR + '}' + localName,
                new Integer(constant));
    }

    static {

        bindXSLTName(XSL_ANALYZE_STRING, "analyze-string");
        bindXSLTName(XSL_APPLY_IMPORTS, "apply-imports");
        bindXSLTName(XSL_APPLY_TEMPLATES, "apply-templates");
        bindXSLTName(XSL_ATTRIBUTE, "attribute");
        bindXSLTName(XSL_ATTRIBUTE_SET, "attribute-set");
        bindXSLTName(XSL_CALL_TEMPLATE, "call-template");
        bindXSLTName(XSL_CHARACTER_MAP, "character-map");
        bindXSLTName(XSL_CHOOSE, "choose");
        bindXSLTName(XSL_COMMENT, "comment");
        bindXSLTName(XSL_COPY, "copy");
        bindXSLTName(XSL_COPY_OF, "copy-of");
        bindXSLTName(XSL_DECIMAL_FORMAT, "decimal-format");
        bindXSLTName(XSL_DOCUMENT, "document");
        bindXSLTName(XSL_ELEMENT, "element");
        bindXSLTName(XSL_FALLBACK, "fallback");
        bindXSLTName(XSL_FOR_EACH, "for-each");
        bindXSLTName(XSL_FOR_EACH_GROUP, "for-each-group");
        bindXSLTName(XSL_FUNCTION, "function");
        bindXSLTName(XSL_IF, "if");
        bindXSLTName(XSL_IMPORT, "import");
        bindXSLTName(XSL_IMPORT_SCHEMA, "import-schema");
        bindXSLTName(XSL_INCLUDE, "include");
        bindXSLTName(XSL_KEY, "key");
        bindXSLTName(XSL_MATCHING_SUBSTRING, "matching-substring");
        bindXSLTName(XSL_MESSAGE, "message");
        bindXSLTName(XSL_NEXT_MATCH, "next-match");
        bindXSLTName(XSL_NUMBER, "number");
        bindXSLTName(XSL_NAMESPACE, "namespace");
        bindXSLTName(XSL_NAMESPACE_ALIAS, "namespace-alias");
        bindXSLTName(XSL_NON_MATCHING_SUBSTRING, "non-matching-substring");
        bindXSLTName(XSL_OTHERWISE, "otherwise");
        bindXSLTName(XSL_OUTPUT, "output");
        bindXSLTName(XSL_OUTPUT_CHARACTER, "output-character");
        bindXSLTName(XSL_PARAM, "param");
        bindXSLTName(XSL_PERFORM_SORT, "perform-sort");
        bindXSLTName(XSL_PRESERVE_SPACE, "preserve-space");
        bindXSLTName(XSL_PROCESSING_INSTRUCTION, "processing-instruction");
        bindXSLTName(XSL_RESULT_DOCUMENT, "result-document");
        bindXSLTName(XSL_SEQUENCE, "sequence");
        bindXSLTName(XSL_SORT, "sort");
        bindXSLTName(XSL_STRIP_SPACE, "strip-space");
        bindXSLTName(XSL_STYLESHEET, "stylesheet");
        bindXSLTName(XSL_TEMPLATE, "template");
        bindXSLTName(XSL_TEXT, "text");
        bindXSLTName(XSL_TRANSFORM, "transform");
        bindXSLTName(XSL_VALUE_OF, "value-of");
        bindXSLTName(XSL_VARIABLE, "variable");
        bindXSLTName(XSL_WITH_PARAM, "with-param");
        bindXSLTName(XSL_WHEN, "when");

        bindXSLTName(XSL_DEFAULT_COLLATION, "default-collation");
        bindXSLTName(XSL_XPATH_DEFAULT_NAMESPACE, "xpath-default-namespace");
        bindXSLTName(XSL_EXCLUDE_RESULT_PREFIXES, "exclude-result-prefixes");
        bindXSLTName(XSL_EXTENSION_ELEMENT_PREFIXES,
                "extension-element-prefixes");
        bindXSLTName(XSL_INHERIT_NAMESPACES, "inherit-namespaces");
        bindXSLTName(XSL_TYPE, "type");
        bindXSLTName(XSL_USE_ATTRIBUTE_SETS, "use-attribute-sets");
        bindXSLTName(XSL_USE_WHEN, "use-when");
        bindXSLTName(XSL_VALIDATION, "validation");
        bindXSLTName(XSL_VERSION, "version");

        bindXMLName(XML_BASE, "base");
        bindXMLName(XML_SPACE, "space");
        bindXMLName(XML_LANG, "lang");
        bindXMLName(XML_ID, "id");

        bindXSName(XS_STRING, "string");
        bindXSName(XS_BOOLEAN, "boolean");
        bindXSName(XS_DECIMAL, "decimal");
        bindXSName(XS_FLOAT, "float");
        bindXSName(XS_DOUBLE, "double");
        bindXSName(XS_DURATION, "duration");
        bindXSName(XS_DATE_TIME, "dateTime");
        bindXSName(XS_TIME, "time");
        bindXSName(XS_DATE, "date");
        bindXSName(XS_G_YEAR_MONTH, "gYearMonth");
        bindXSName(XS_G_YEAR, "gYear");
        bindXSName(XS_G_MONTH_DAY, "gMonthDay");
        bindXSName(XS_G_DAY, "gDay");
        bindXSName(XS_G_MONTH, "gMonth");
        bindXSName(XS_HEX_BINARY, "hexBinary");
        bindXSName(XS_BASE64_BINARY, "base64Binary");
        bindXSName(XS_ANY_URI, "anyURI");
        bindXSName(XS_QNAME, "QName");
        bindXSName(XS_NOTATION, "NOTATION");
        bindXSName(XS_INTEGER, "integer");
        bindXSName(XS_NON_POSITIVE_INTEGER, "nonPositiveInteger");
        bindXSName(XS_NEGATIVE_INTEGER, "negativeInteger");
        bindXSName(XS_LONG, "long");
        bindXSName(XS_INT, "int");
        bindXSName(XS_SHORT, "short");
        bindXSName(XS_BYTE, "byte");
        bindXSName(XS_NON_NEGATIVE_INTEGER, "nonNegativeInteger");
        bindXSName(XS_POSITIVE_INTEGER, "positiveInteger");
        bindXSName(XS_UNSIGNED_LONG, "unsignedLong");
        bindXSName(XS_UNSIGNED_INT, "unsignedInt");
        bindXSName(XS_UNSIGNED_SHORT, "unsignedShort");
        bindXSName(XS_UNSIGNED_BYTE, "unsignedByte");
        bindXSName(XS_NORMALIZED_STRING, "normalizedString");
        bindXSName(XS_TOKEN, "token");
        bindXSName(XS_LANGUAGE, "language");
        bindXSName(XS_NMTOKEN, "NMTOKEN");
        bindXSName(XS_NMTOKENS, "NMTOKENS"); // NB: list type
        bindXSName(XS_NAME, "Name");
        bindXSName(XS_NCNAME, "NCName");
        bindXSName(XS_ID, "ID");
        bindXSName(XS_IDREF, "IDREF");
        bindXSName(XS_IDREFS, "IDREFS"); // NB: list type
        bindXSName(XS_ENTITY, "ENTITY");
        bindXSName(XS_ENTITIES, "ENTITIES"); // NB: list type

        bindXSName(XS_ANY_TYPE, "anyType");
        bindXSName(XS_ANY_SIMPLE_TYPE, "anySimpleType");
        bindXSName(XS_INVALID_NAME, "invalidName");

        bindXSName(XS_ALL, "all");
        bindXSName(XS_ANNOTATION, "annotation");
        bindXSName(XS_ANY, "any");
        bindXSName(XS_ANY_ATTRIBUTE, "anyAttribute");
        bindXSName(XS_APPINFO, "appinfo");
        bindXSName(XS_ASSERT, "assert");
        bindXSName(XS_ATTRIBUTE, "attribute");
        bindXSName(XS_ATTRIBUTE_GROUP, "attributeGroup");
        bindXSName(XS_CHOICE, "choice");
        bindXSName(XS_COMPLEX_CONTENT, "complexContent");
        bindXSName(XS_COMPLEX_TYPE, "complexType");
        bindXSName(XS_DOCUMENTATION, "documentation");
        bindXSName(XS_ELEMENT, "element");
        bindXSName(XS_ENUMERATION, "enumeration");
        bindXSName(XS_EXTENSION, "extension");
        bindXSName(XS_FIELD, "field");
        bindXSName(XS_FRACTION_DIGITS, "fractionDigits");
        bindXSName(XS_GROUP, "group");
        bindXSName(XS_IMPORT, "import");
        bindXSName(XS_INCLUDE, "include");
        bindXSName(XS_KEY, "key");
        bindXSName(XS_KEYREF, "keyref");
        bindXSName(XS_LENGTH, "length");
        bindXSName(XS_LIST, "list");
        bindXSName(XS_MAX_EXCLUSIVE, "maxExclusive");
        bindXSName(XS_MAX_INCLUSIVE, "maxInclusive");
        bindXSName(XS_MAX_LENGTH, "maxLength");
        bindXSName(XS_MIN_EXCLUSIVE, "minExclusive");
        bindXSName(XS_MIN_INCLUSIVE, "minInclusive");
        bindXSName(XS_MIN_LENGTH, "minLength");
        bindXSName(XS_notation, "notation");
        bindXSName(XS_PATTERN, "pattern");
        bindXSName(XS_REDEFINE, "redefine");
        // bindXSName(XS_REPORT, "report");
        bindXSName(XS_RESTRICTION, "restriction");
        bindXSName(XS_SCHEMA, "schema");
        bindXSName(XS_SELECTOR, "selector");
        bindXSName(XS_SEQUENCE, "sequence");
        bindXSName(XS_SIMPLE_CONTENT, "simpleContent");
        bindXSName(XS_SIMPLE_TYPE, "simpleType");
        bindXSName(XS_TOTAL_DIGITS, "totalDigits");
        bindXSName(XS_UNION, "union");
        bindXSName(XS_UNIQUE, "unique");
        bindXSName(XS_WHITE_SPACE, "whiteSpace");

        bindXSName(XS_UNTYPED, "untyped");
        bindXSName(XS_UNTYPED_ATOMIC, "untypedAtomic");
        bindXSName(XS_ANY_ATOMIC_TYPE, "anyAtomicType");
        bindXSName(XS_YEAR_MONTH_DURATION, "yearMonthDuration");
        bindXSName(XS_DAY_TIME_DURATION, "dayTimeDuration");
        bindXSName(XS_NUMERIC, "_numeric_");

        bindXSIName(XSI_TYPE, "type");
        bindXSIName(XSI_NIL, "nil");
        bindXSIName(XSI_SCHEMA_LOCATION, "schemaLocation");
        bindXSIName(XSI_NO_NAMESPACE_SCHEMA_LOCATION,
                "noNamespaceSchemaLocation");
        bindXSIName(XSI_SCHEMA_LOCATION_TYPE, "anonymous_schemaLocationType");

        // Cedar
        bindCEDARName(CEDAR_FEATURE, STR_CEDAR_FEATURE);
        bindCEDARName(CEDAR_IMPORT, STR_CEDAR_IMPORT);
        bindCEDARName(CEDAR_DECLARATION, STR_CEDAR_DECLARATION);
        bindCEDARName(CEDAR_FEATUREUI, STR_CEDAR_FEATUREUI);
        bindCEDARName(CEDAR_FLOW, STR_CEDAR_FLOW);
        bindCEDARName(CEDAR_NAME, STR_CEDAR_NAME);
        bindCEDARName(CEDAR_VERSION, STR_CEDAR_VERSION);
        bindCEDARName(CEDAR_VARIABLE, STR_CEDAR_VARIABLE);
        bindCEDARName(CEDAR_ID, STR_CEDAR_ID);
        bindCEDARName(CEDAR_TITLE, STR_CEDAR_TITLE);
        bindCEDARName(CEDAR_WINDOW, STR_CEDAR_WINDOW);
        bindCEDARName(CEDAR_FORM, STR_CEDAR_FORM);
        bindCEDARName(CEDAR_FIELDSET, STR_CEDAR_FIELDSET);
        bindCEDARName(CEDAR_LABEL, STR_CEDAR_LABEL);
        bindCEDARName(CEDAR_COMBO, STR_CEDAR_COMBO);
        bindCEDARName(CEDAR_VALUE, STR_CEDAR_VALUE);
        bindCEDARName(CEDAR_CHECKBOXGROUP, STR_CEDAR_CHECKBOXGROUP);
        bindCEDARName(CEDAR_LISTFIELD, STR_CEDAR_LISTFIELD);
        bindCEDARName(CEDAR_COMPOSITE, STR_CEDAR_COMPOSITE);
        bindCEDARName(CEDAR_COMBOITEM, STR_CEDAR_COMBOITEM);
        bindCEDARName(CEDAR_SELECT, STR_CEDAR_SELECT);
        bindCEDARName(CEDAR_SEQUENCE, STR_CEDAR_SEQUENCE);
        bindCEDARName(CEDAR_TASKLET, STR_CEDAR_TASKLET);
        bindCEDARName(CEDAR_MACHINE, STR_CEDAR_MACHINE);
        bindCEDARName(CEDAR_PROPERTIES, STR_CEDAR_PROPERTIES);
        bindCEDARName(CEDAR_PROPERTY, STR_CEDAR_PROPERTY);
        bindCEDARName(CEDAR_COUNT, STR_CEDAR_COUNT);
        bindCEDARName(CEDAR_CPU, STR_CEDAR_CPU);
        bindCEDARName(CEDAR_MEM, STR_CEDAR_MEM);
        bindCEDARName(CEDAR_DISK, STR_CEDAR_DISK);
        bindCEDARName(CEDAR_CASE, STR_CEDAR_CASE);
        bindCEDARName(CEDAR_CASES, STR_CEDAR_CASES);
        bindCEDARName(CEDAR_PROVIDER, STR_CEDAR_PROVIDER);
        bindCEDARName(CEDAR_PUBLIC, STR_CEDAR_PUBLIC);
        bindCEDARName(CEDAR_VARIABLES, STR_CEDAR_VARIABLES);
        bindCEDARName(CEDAR_TASKLETS, STR_CEDAR_TASKLETS);
        bindCEDARName(CEDAR_VALUES, STR_CEDAR_VALUES);
        bindCEDARName(CEDAR_CONTRIBUTER, STR_CEDAR_CONTRIBUTER);
        bindCEDARName(CEDAR_PARALLEL, STR_CEDAR_PARALLEL);
        bindCEDARName(CEDAR_MIN, STR_CEDAR_MIN);
        bindCEDARName(CEDAR_MAX, STR_CEDAR_MAX);
        bindCEDARName(CEDAR_BIND, STR_CEDAR_BIND);
        bindCEDARName(CEDAR_XMLNS, STR_CEDAR_XMLNS);
        bindCEDARName(CEDAR_TEXTFIELD, STR_CEDAR_TEXTFIELD);
        bindCEDARName(CEDAR_TEXTAREA, STR_CEDAR_TEXTAREA);
        bindCEDARName(CEDAR_SHOWONSELECT, STR_CEDAR_SHOWONSELECT);
        bindCEDARName(CEDAR_DEPENDS, STR_CEDAR_DEPENDS);
        bindCEDARName(CEDAR_DEFAULT, STR_CEDAR_DEFAULT);
        bindCEDARName(CEDAR_FILEUPLOADFIELD, STR_CEDAR_FILEUPLOADFIELD);
        bindCEDARName(CEDAR_SVN, STR_CEDAR_SVN);
        bindCEDARName(CEDAR_URL, STR_CEDAR_URL);
        bindCEDARName(CEDAR_REV, STR_CEDAR_REV);
        bindCEDARName(CEDAR_LOG, STR_CEDAR_LOG);
        bindCEDARName(CEDAR_DEPEND, STR_CEDAR_DEPEND);
        bindCEDARName(CEDAR_REFID, STR_CEDAR_REFID);
        bindCEDARName(CEDAR_ACTION, STR_CEDAR_ACTION);
        bindCEDARName(CEDAR_TESTCASES, STR_CEDAR_TESTCASES);
        bindCEDARName(CEDAR_TESTCASE, STR_CEDAR_TESTCASE);
        bindCEDARName(CEDAR_KEYS, STR_CEDAR_KEYS);
        bindCEDARName(CEDAR_KEY, STR_CEDAR_KEY);
        bindCEDARName(CEDAR_ITEMS, STR_CEDAR_ITEMS);
        bindCEDARName(CEDAR_ITEM, STR_CEDAR_ITEM);
        bindCEDARName(CEDAR_GLOBAL, STR_CEDAR_GLOBAL);
        bindCEDARName(CEDAR_TOOL, STR_CEDAR_TOOL);
        bindCEDARName(CEDAR_CASEPARSER, STR_CEDAR_CASEPARSER);
        bindCEDARName(CEDAR_LEVEL, STR_CEDAR_LEVEL);
        bindCEDARName(CEDAR_PARAMS, STR_CEDAR_PARAMS);
        bindCEDARName(CEDAR_PARAM, STR_CEDAR_PARAM);
        bindCEDARName(CEDAR_TIMEOUT, STR_CEDAR_TIMEOUT);
        bindCEDARName(CEDAR_SUBMIT, STR_CEDAR_SUBMIT);
        bindCEDARName(CEDAR_FORMITEM, STR_CEDAR_FORMITEM);
        bindCEDARName(CEDAR_PARSER, STR_CEDAR_PARSER);
        bindCEDARName(CEDAR_OS, STR_CEDAR_OS);
        bindCEDARName(CEDAR_ARCH, STR_CEDAR_ARCH);
        bindCEDARName(CEDAR_HOST, STR_CEDAR_HOST);
        bindCEDARName(CEDAR_SHARABLE, STR_CEDAR_SHARABLE);
        bindCEDARName(CEDAR_ONFAIL, STR_CEDAR_ONFAIL);

        bindCEDARName(CEDAR_LAUNCHES, STR_CEDAR_LAUNCHES);
        bindCEDARName(CEDAR_LAUNCHSET, STR_CEDAR_LAUNCHSET);
        bindCEDARName(CEDAR_LAUNCH, STR_CEDAR_LAUNCH);
        bindCEDARName(CEDAR_OPTION, STR_CEDAR_OPTION);
        bindCEDARName(CEDAR_TRIGGERS, STR_CEDAR_TRIGGERS);
        bindCEDARName(CEDAR_TRIGGER, STR_CEDAR_TRIGGER);
        bindCEDARName(CEDAR_SHORTCUTS, STR_CEDAR_SHORTCUTS);
        bindCEDARName(CEDAR_SHORTCUT, STR_CEDAR_SHORTCUT);
        bindCEDARName(CEDAR_DEBUG, STR_CEDAR_DEBUG);
        bindCEDARName(CEDAR_RECYCLE, STR_CEDAR_RECYCLE);
        bindCEDARName(CEDAR_INTERVAL, STR_CEDAR_INTERVAL);
        bindCEDARName(CEDAR_GIT, STR_CEDAR_GIT);
        bindCEDARName(CEDAR_BRANCH, STR_CEDAR_BRANCH);
        bindCEDARName(CEDAR_VISIBLE, STR_CEDAR_VISIBLE);
    }

    /**
     * Get the fingerprint of a system-defined name, from its URI and local name
     * 
     * @param uri
     *            the namespace URI
     * @param localName
     *            the local part of the name
     * @return the standard fingerprint, or -1 if this is not a built-in name
     */

    public static int getFingerprint(String uri, String localName) {
        Integer fp = (Integer) lookup.get('{' + uri + '}' + localName);
        if (fp == null) {
            return -1;
        } else {
            return fp.intValue();
        }
    }

    /**
     * Get the local part of a system-defined name
     * 
     * @param fingerprint
     *            the fingerprint of the name
     * @return the local part of the name
     */

    public static String getLocalName(int fingerprint) {
        return localNames[fingerprint];
    }

    /**
     * Get the namespace URI part of a system-defined name
     * 
     * @param fingerprint
     *            the fingerprint of the name
     * @return the namespace URI part of the name
     */

    public static String getURI(int fingerprint) {
        int c = fingerprint >> 7;
        switch (c) {
        case DFLT_NS:
            return "";
        case XSL_NS:
            return NamespaceConstant.XSLT;
        case XML_NS:
            return NamespaceConstant.XML;
        case XS_NS:
            return NamespaceConstant.SCHEMA;
        case XSI_NS:
            return NamespaceConstant.SCHEMA_INSTANCE;
        case CEDAR_NS:
            return NamespaceConstant.CEDAR;
        default:
            return null;
        }
    }

    /**
     * Get the namespace URI part of a system-defined name as a URI code
     * 
     * @param fingerprint
     *            the fingerprint of the name
     * @return the namespace URI part of the name, as a URI code
     */

    public static short getURICode(int fingerprint) {
        int c = fingerprint >> 7;
        switch (c) {
        case DFLT_NS:
            return 0;
        case XSL_NS:
            return NamespaceConstant.XSLT_CODE;
        case XML_NS:
            return NamespaceConstant.XML_CODE;
        case XS_NS:
            return NamespaceConstant.SCHEMA_CODE;
        case XSI_NS:
            return NamespaceConstant.XSI_CODE;
        case CEDAR_NS:
            return NamespaceConstant.CEDAR_CODE;
        default:
            return -1;
        }
    }

    /**
     * Get the Clark form of a system-defined name, given its name code or
     * fingerprint
     * 
     * @param fingerprint
     *            the fingerprint of the name
     * @return the local name if the name is in the null namespace, or
     *         "{uri}local" otherwise. The name is always interned.
     */

    public static String getClarkName(int fingerprint) {
        String uri = getURI(fingerprint);
        if (uri.length() == 0) {
            return getLocalName(fingerprint);
        } else {
            return '{' + uri + '}' + getLocalName(fingerprint);
        }
    }

    /**
     * Get the conventional prefix of a system-defined name
     * 
     * @param fingerprint
     *            the fingerprint of the name
     * @return the conventional prefix of the name
     */

    public static String getPrefix(int fingerprint) {
        int c = fingerprint >> 7;
        switch (c) {
        case DFLT_NS:
            return "";
        case XSL_NS:
            return "xsl";
        case XML_NS:
            return "xml";
        case XS_NS:
            return "xs";
        case XSI_NS:
            return "xsi";
        case CEDAR_NS:
            return "cedar";
        default:
            return null;
        }
    }

    /**
     * Get the lexical display form of a system-defined name
     * 
     * @param fingerprint
     *            the fingerprint of the name
     * @return the lexical display form of the name, using a conventional prefix
     */

    public static String getDisplayName(int fingerprint) {
        if (fingerprint == -1) {
            return "(anonymous type)";
        }
        if (fingerprint > 1023) {
            return "(" + fingerprint + ')';
        }
        if ((fingerprint >> 7) == DFLT) {
            return getLocalName(fingerprint);
        }
        return getPrefix(fingerprint) + ':' + getLocalName(fingerprint);
    }
}

//
// The contents of this file are subject to the Mozilla Public License Version
// 1.0 (the "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none
//