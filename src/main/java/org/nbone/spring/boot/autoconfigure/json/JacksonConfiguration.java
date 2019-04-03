package org.nbone.spring.boot.autoconfigure.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.nbone.util.DateFPUtils;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.Date;

/**
 *
 * 自定义 json 序列化和反序列化特性
 * @author chenyicheng
 * @version 1.0
 * @since 2018/4/10
 *
 */
@JsonComponent
public class JacksonConfiguration {

    /*public static class Serializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException, JsonProcessingException {


        }
    }*/

    /**
     * yyyy-MM-dd HH:mm:ss format / 时间戳 1540395926000 format
     *
     */
    public static class Deserializer extends JsonDeserializer<Date> {
        static {
            System.out.println("=========================JacksonConfiguration.Deserializer class init.");
        }
        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JsonProcessingException {
            String text = jsonParser.getText();
            Date date =  DateFPUtils.parseDateMultiplePattern(text);

            return date;
        }
    }

}