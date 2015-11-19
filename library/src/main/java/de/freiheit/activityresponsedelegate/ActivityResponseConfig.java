package de.freiheit.activityresponsedelegate;

/**
 *
 * Created by timfreiheit on 19.11.15.
 */
public class ActivityResponseConfig {

    private boolean enableDebugLogs = false;

    ActivityResponseConfig(Builder builder) {
        enableDebugLogs = builder.enableDebugLogs;
    }

    public boolean isDebugLogsEnabled(){
        return enableDebugLogs;
    }

    public static class Builder {

        boolean enableDebugLogs = false;

        public Builder setEnableDebugLogs(boolean enable){
            this.enableDebugLogs = enable;
            return this;
        }

        public ActivityResponseConfig build(){
            return new ActivityResponseConfig(this);
        }
    }
}
