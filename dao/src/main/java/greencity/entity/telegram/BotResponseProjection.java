package greencity.entity.telegram;

public interface BotResponseProjection {
    Long getId();

    String getMessageType();

    String getLang();

    String getText();
}
