package io.github.bonigarcia.wdm.online;

import com.google.gson.JsonSyntaxException;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.github.bonigarcia.wdm.online.Parser.parseJson;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    public void testBrokenJSON() throws IOException {

        try {
            // known HTML endpoint
            LastGoodVersions x = parseJson(new HttpClient(new Config()), "https://www.example.com/", LastGoodVersions.class);
            fail("should have barfed");
        } catch (WebDriverManagerException e) {
            assertEquals("Bad JSON. First 100 chars <!doctype html><html><head>    <title>Example Domain</title>    <meta charset=\"utf-8\" />    <meta ht", e.getMessage());
            assertSame(e.getCause().getClass(), JsonSyntaxException.class);
        }

    }

}