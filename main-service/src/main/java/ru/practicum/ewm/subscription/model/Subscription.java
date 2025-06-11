package ru.practicum.ewm.subscription.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewm.user.model.User;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@ToString(exclude = {"follower", "followed"})
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    @NotNull(message = "Follower must not be null")
    private User follower;

    @ManyToOne
    @JoinColumn(name = "followed_id")
    @NotNull(message = "Followed must not be null")
    private User followed;
}
