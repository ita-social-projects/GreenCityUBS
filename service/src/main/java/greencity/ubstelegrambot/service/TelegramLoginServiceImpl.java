package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.dto.TestersSignInRequest;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.user.employee.Employee;
import greencity.enums.MessageType;
import greencity.exceptions.BadRequestException;
import greencity.repository.EmployeeRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.service.ubs.TelegramBotResponseService;
import greencity.service.ubs.TelegramLoginService;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramLoginServiceImpl implements TelegramLoginService {
    private final TelegramManagerRepository telegramManagerRepository;
    private final EmployeeRepository employeeRepository;
    private final TelegramUtils telegramUtils;
    private final TelegramBotResponseService telegramBotResponseService;
    private final UserRemoteClient userRemoteClient;
    @Value("${greencity.sing-in.secret-token}")
    private String secretToken;
    private static final String USERNAME = "username";

    /**
     * {@inheritDoc}
     */
    @Override
    public void logoutManager(String chatId) {
        telegramManagerRepository.findByChatId(chatId)
            .ifPresent(telegramManagerRepository::delete);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processInputManagerCredentialsRequest(Message message, String lang) {
        String[] parts = message.getText().split(":");

        String template = telegramBotResponseService.getResponseByLangAndMessageType(lang, MessageType.LOGIN_ERROR);

        if (parts.length < 2) {
            String errorText = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.INCORRECT_LOGIN_FORMAT);
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                errorText, lang, template);
        }

        String login = parts[0];
        String password = parts[1];

        Optional<Employee> employee = employeeRepository.findByEmailWithPositions(login);

        if (employee.isEmpty()) {
            String errorText = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.USER_NOT_EMPLOYEE);
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                errorText, lang, template);
        }

        boolean isManager = telegramUtils.checkIsEmployeeManager(employee.get());

        if (!isManager) {
            String errorText = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.EMPLOYEE_NOT_MANAGER);
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                errorText, lang, template);
        }

        try {
            var response = userRemoteClient.signIn(new TestersSignInRequest(login, password, secretToken, "PICKUP"));

            var responseBody = response.getBody();
            String name = (responseBody != null && responseBody.name() != null) ? responseBody.name() : USERNAME;

            telegramManagerRepository.save(
                TelegramManager
                    .builder()
                    .chatId(message.getChatId().toString())
                    .employee(employee.get())
                    .build());

            String successText = telegramBotResponseService.getResponseByLangAndMessageType(lang,
                MessageType.LOGIN_SUCCESS);
            return MessageFactory.createSuccessLoginMessage(message.getChatId().toString(), name,
                lang, successText);
        } catch (BadRequestException e) {
            String errorText = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.LOGIN_FAILED);
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                errorText, lang, template);
        } catch (Exception e) {
            String errorText = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.SOMETHING_WENT_WRONG);
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                errorText, lang, template);
        }
    }
}