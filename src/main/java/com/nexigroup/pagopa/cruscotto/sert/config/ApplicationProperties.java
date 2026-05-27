package com.nexigroup.pagopa.cruscotto.sert.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to PagoPa Cruscotto Sert Backend.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 */
@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = true)
public class ApplicationProperties {


    @Setter
    private boolean enableCsrf;

    private final Password password = new Password();

    private final Job job = new Job();

    private final Quartz quartz = new Quartz();

    private final PagoPaClient pagoPaClient = new PagoPaClient();

    private final AuthGroup authGroup = new AuthGroup();

    @Setter
    @Getter
    public static class Password {

        private Integer dayPasswordExpired;

        private Integer failedLoginAttempts;

        private Integer hoursKeyResetPasswordExpired;
    }

    @Getter
    @Setter
    public static class Job {

        private LoadTaxonomyJob loadTaxonomyJob = new LoadTaxonomyJob();

        private LoadMaintenanceJob loadMaintenanceJob = new LoadMaintenanceJob();

        private LoadRegistryJob loadRegistryJob = new LoadRegistryJob();

        private KpiA1Job kpiA1Job = new KpiA1Job();

        private KpiA2Job kpiA2Job = new KpiA2Job();

        private KpiB2Job kpiB2Job = new KpiB2Job();

        private KpiB9Job kpiB9Job = new KpiB9Job();

        private KpiB3Job kpiB3Job = new KpiB3Job();

        private KpiB4Job kpiB4Job = new KpiB4Job();

        private KpiB5Job kpiB5Job = new KpiB5Job();

        private KpiB1Job kpiB1Job = new KpiB1Job();

        private KpiB8Job kpiB8Job = new KpiB8Job();

        private KpiC2Job kpiC2Job = new KpiC2Job();

        private KpiB6Job kpiB6Job = new KpiB6Job();

        private KpiC1Job kpiC1Job = new KpiC1Job();

        private LoadStandInDataJob loadStandInDataJob = new LoadStandInDataJob();

        private ClearLogJob clearLogJob = new ClearLogJob();

        private ReportGenerationJob reportGenerationJob = new ReportGenerationJob();
    }

    @Getter
    @Setter
    public static class LoadTaxonomyJob {

        private boolean enabled;

        private String cron;
    }

    @Getter
    @Setter
    public static class LoadMaintenanceJob {

        private boolean enabled;

        private String cron;
    }

    @Getter
    @Setter
    public static class LoadRegistryJob {

        private boolean enabled;

        private String cron;
    }

    @Getter
    @Setter
    public static class KpiA1Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class KpiA2Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class KpiB2Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class KpiB9Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class KpiB3Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class KpiB4Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class KpiB5Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class KpiB1Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class KpiB8Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class KpiC2Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }


    @Getter
    @Setter
    public static class KpiB6Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class KpiC1Job {

        private boolean enabled = true;

        private String cron;

        private int limit;
    }

    @Getter
    @Setter
    public static class ClearLogJob {

        private boolean enabled;

        private String cron;

        private Integer days;
    }

    @Getter
    @Setter
    public static class Quartz {

        private Boolean exposeSchedulerInRepository;

        private Boolean activeDbLog;
    }

    @Setter
    @Getter
    public static class PagoPaClient {

        private String proxyHost;

        private Integer proxyPort;

        private Api taxonomy;

        private Api backOffice;

        private Api cache;

        private Api standIn;

        @Setter
        @Getter
        public static class Api {

            private String url;

            private String apiKeyName;

            private String apiKeyValue;
        }
    }

    @Setter
    @Getter
    public static class LoadStandInDataJob {

        private boolean enabled;

        private String cronExpression = "0 0 0 * * ?"; // Every day at midnight

        private Integer chunkSizeDays = 10; // Default: 10 days per chunk

        private Integer initializationMonths = 6; // Default: 6 months of historical data

        private Integer parallelChunks = 3; // Default: 3 chunk in parallelo (numero di thread nel pool)

        private Integer delayBetweenChunkStartsMs = 1000; // Default: 1 secondo di delay tra l'avvio di ogni chunk (per evitare rate limiting)

        private Integer pollingIntervalMs = 2000; // Default: 2 seconds (2000ms) polling interval to check job status
    }

    @Setter
    @Getter
    public static class ReportGenerationJob {

        private boolean enabled = true;

        private String cron = "0 */10 * * * ?"; // Every 10 minutes
    }

    @Setter
    @Getter
    public static class AuthGroup {

        private String superAdmin;
    }
}
