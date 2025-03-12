package tythor.herakia.annotation.executiontime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.event.Level;
import org.springframework.core.annotation.AnnotationUtils;
import tythor.herakia.utility.SignatureUtil;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ExecutionTimeConfig {
    public static final String KEY = ExecutionTimeConfig.class.getName();

    @Setter(AccessLevel.NONE)
    private String signature;
    private String tag;
    private Level level;
    private Long logFrequency;
    private Boolean reset;
    private Boolean disableLog;

    public ExecutionTimeConfig() {
        this(SignatureUtil.getSimpleSignature(2));
    }

    public ExecutionTimeConfig(String signature) {
        this.signature = signature;

        ExecutionTime executionTime = AnnotationUtils.synthesizeAnnotation(ExecutionTime.class);
        this.tag = executionTime.tag();
        this.level = executionTime.level();
        this.logFrequency = executionTime.logFrequency();
        this.reset = executionTime.reset();
        this.disableLog = executionTime.disableLog();
    }
}
