package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QuartzConstants {
    public static final Long MONOBANK_LINK_VALIDITY_SECONDS = 30L;
    public static final Long WAY_FOR_PAY_LINK_VALIDITY_SECONDS = 30L;
    public static final String PAYMENT_EXPIRY_JOB_GROUP = "paymentExpiry";
    public static final String PAYMENT_EXPIRY_JOB_KEY = PAYMENT_EXPIRY_JOB_GROUP + "-";
    public static final String PAYMENT_EXPIRY_TRIGGER_KEY = PAYMENT_EXPIRY_JOB_GROUP + "Trigger-";
    public static final String PAYMENT_EXPIRY_SCHEDULE_EXCEPTION = "Couldn't schedule payment expiry job";
    public static final String PAYMENT_EXPIRY_CANCEL_EXCEPTION = "Couldn't cancel payment expiry job";
    public static final String PAYMENT_EXPIRY_JOB_NOT_FOUND_EXCEPTION = "Couldn't find payment expiry job for order with id: ";
    public static final String QUARTZ_SCHEDULER_EXCEPTION = "Unexpected exception thrown when working with scheduler";
}
