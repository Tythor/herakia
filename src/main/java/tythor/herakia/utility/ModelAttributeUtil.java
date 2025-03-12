package tythor.herakia.utility;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.util.UriBuilder;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public class ModelAttributeUtil {
    public static UriBuilder addParamsToBuilder(Object modelAttribute, UriBuilder uriBuilder) {
        if (modelAttribute == null) return uriBuilder;

        Map<String, Object> params = SpringUtil.getBean(ObjectMapper.class).convertValue(modelAttribute, new TypeReference<>() {});
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            if (ObjectUtils.isNotEmpty(fieldValue)) {
                uriBuilder.queryParam(fieldName, fieldValue);
            }
        }

        return uriBuilder;
    }
}
