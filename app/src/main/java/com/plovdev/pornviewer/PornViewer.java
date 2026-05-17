package com.plovdev.pornviewer;

import com.plovdev.pornviewer.pvvasupport.parser.ScriptEngineExecutor;
import org.plovdev.pvva.models.parsers.MainParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PornViewer {
    private static final Logger log = LoggerFactory.getLogger(PornViewer.class);

    static void main(String[] args) throws Exception {
        String code = """
                function parseHtml(htmlString)
                       local doc = html.parse(htmlString)
                       local img = doc.select("li.video_block img")
                       if img and #img > 0 then
                           return img[1].attr("src")
                       end
                       return nil
                end
                """;

        ScriptEngineExecutor engine = new ScriptEngineExecutor(new MainParser(code));
        String response = engine.execute("parseHtml", """
                <li id="48934" class="video_block trailer">
                    <a class="image" href="http://example.com/movie/48934">
                        <div class="tumba">
                            <img src="https://example.com/photo1.webp"
                                 width="450" height="265" alt="photo1" loading="lazy">
                        </div>
                        <p>Title</p>
                    </a>
                    <span class="duration">23:03</span>
                    <span class="video_views">145,329</span>
                    <span class="video_comments">25</span>
                    <div onclick="addToFavoriteSmall(48934,0,this);" title="Добавить в закладки" class="small_fav_add  tooltip">
                        <span></span>
                    </div>
                    <span class="mini-rating tooltip" title="Голосов: 535">85%</span>
                </li>
                """);
        System.out.println(response);
    }
}