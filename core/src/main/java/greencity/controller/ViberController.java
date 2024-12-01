package greencity.controller;

import greencity.dto.viber.dto.CallbackDto;
import greencity.dto.viber.enums.EventTypes;
import greencity.service.ubs.ViberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ViberController {
    private final ViberService viberService;

    /**
     * The method sets the URL of the Viberr bot to which Viber requests will be
     * sent.
     *
     * @return {@link String} - which contains the status of success or failure.
     */
    @GetMapping(value = "/setwebhook", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> setWebHook() {
        return viberService.setWebhook();
    }

    /**
     * The method removes Viber bot url.
     *
     * @return {@link String} - which contains the status of success or failure.
     */
    @GetMapping(value = "/removewebhook", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> removeWebHook() {
        return viberService.removeWebHook();
    }

    /**
     * The method receives all requestss from Viberr.
     *
     * @param callbackDto - contains all the necessary data.
     */
    @PostMapping(value = "/bot", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void callBack(@RequestBody CallbackDto callbackDto) {
        if (EventTypes.conversation_started.toString().equals(callbackDto.getEvent())) {
            viberService.sendWelcomeMessageAndPreRegisterViberBotForUser(callbackDto.getUser().getId(),
                callbackDto.getContext());
        }
        if (EventTypes.message.toString().equals(callbackDto.getEvent())) {
            viberService.sendMessageAndRegisterViberBotForUser(callbackDto.getSender().getId());
        }
    }

    /**
     * The method allows to see info about Viber bot.
     *
     * @return @return {@link String} - which contains the status of success or
     *         failure.
     */
    @GetMapping(value = "/accountinfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> getAccountInfo() {
        return viberService.getAccountInfo();
    }
}
