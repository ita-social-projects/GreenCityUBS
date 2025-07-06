package greencity.entity.telegram;

import greencity.entity.user.User;
import greencity.enums.ChatState;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"messages", "feedbacks", "user"})
@EqualsAndHashCode(exclude = {"messages", "feedbacks", "user"})
public class TelegramChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String chatId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatState chatState = ChatState.NORMAL;

    @Column(nullable = false, name = "notify")
    private Boolean isNotify;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TelegramMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatFeedback> feedbacks = new ArrayList<>();
}
