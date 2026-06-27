package com.plovdev.pornviewer.pvvasupport.parser.lualibs;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.jspecify.annotations.NonNull;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class HtmlLib extends TwoArgFunction {
    @Override
    public LuaValue call(@NonNull LuaValue luaValue, @NonNull LuaValue env) {
        LuaValue library = new LuaTable();
        library.set("parseHtml", new parse(true));
        library.set("parseXml", new parse(false));

        env.set("html", library);
        return library;
    }

    private class parse extends OneArgFunction {
        private final boolean isHtml;

        public parse(boolean isHtml) {
            this.isHtml = isHtml;
        }

        @Override
        public @NonNull LuaValue call(@NonNull LuaValue luaValue) {
            String content = luaValue.checkjstring();
            Document document;

            if (isHtml) {
                document = Jsoup.parse(content);
            } else {
                document = Jsoup.parse(content, "", Parser.xmlParser());
            }

            LuaValue luaDoc = new LuaTable();

            luaDoc.set("body", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return elementToLua(document.body());
                }
            });

            luaDoc.set("head", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return elementToLua(document.head());
                }
            });

            luaDoc.set("location", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    String location = document.location();
                    return LuaValue.valueOf(location);
                }
            });

            luaDoc.set("nodeName", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(document.nodeName());
                }
            });

            luaDoc.set("outerHtml", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(document.outerHtml());
                }
            });

            luaDoc.set("text", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(document.text());
                }
            });

            luaDoc.set("title", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    String title = document.title();
                    return LuaValue.valueOf(title);
                }
            });

            luaDoc.set("select", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue selector) {
                    Elements elements = document.select(selector.checkjstring());
                    LuaValue result = LuaValue.tableOf();
                    int index = 1;
                    for (Element el : elements) {
                        result.set(index++, elementToLua(el));
                    }
                    return result;
                }
            });

            luaDoc.set("selectFirst", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue selector) {
                    Element el = document.selectFirst(selector.checkjstring());
                    return el != null ? elementToLua(el) : LuaValue.NIL;
                }
            });

            luaDoc.set("getElementById", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue id) {
                    Element el = document.getElementById(id.checkjstring());
                    return el != null ? elementToLua(el) : LuaValue.NIL;
                }
            });

            return luaDoc;
        }
    }

    private LuaValue elementToLua(Element element) {
        if (element == null) {
            return LuaValue.NIL;
        }
        LuaValue el = new LuaTable();

        el.set("tag", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(element.tagName());
            }
        });
        el.set("nodeName", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(element.nodeName());
            }
        });
        el.set("id", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(element.id());
            }
        });
        el.set("className", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(element.className());
            }
        });
        el.set("html", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(element.html());
            }
        });
        el.set("outerHtml", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(element.outerHtml());
            }
        });
        el.set("text", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(element.text());
            }
        });
        el.set("ownText", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(element.ownText());
            }
        });

        el.set("attr", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                return LuaValue.valueOf(element.attr(key.checkjstring()));
            }
        });

        el.set("hasAttr", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                return LuaValue.valueOf(element.hasAttr(key.checkjstring()));
            }
        });

        el.set("children", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                LuaValue children = LuaValue.tableOf();
                int i = 1;
                for (Element child : element.children()) {
                    children.set(i++, elementToLua(child));
                }
                return children;
            }
        });

        el.set("child", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue index) {
                int idx = index.checkint();
                Element child = element.child(idx);
                return elementToLua(child);
            }
        });

        el.set("childrenSize", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(element.children().size());
            }
        });

        el.set("parent", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return elementToLua(element.parent());
            }
        });

        el.set("select", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue selector) {
                Elements elements = element.select(selector.checkjstring());
                LuaValue result = LuaValue.tableOf();
                int i = 1;
                for (Element e : elements) {
                    result.set(i++, elementToLua(e));
                }
                return result;
            }
        });

        el.set("selectFirst", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue selector) {
                Element e = element.selectFirst(selector.checkjstring());
                return e != null ? elementToLua(e) : LuaValue.NIL;
            }
        });

        return el;
    }
}