package me.morpheus.lighteconomy.account;

import me.morpheus.lighteconomy.util.NameUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.util.Objects;
import java.util.UUID;

public final class LEUniqueAccount extends LEAccount implements UniqueAccount {

    private final UUID uniqueId;

    LEUniqueAccount(LEAccount.AccountBuilder builder) {
        super(builder);
        Objects.requireNonNull(builder.uniqueId, "uuid is null");
        this.uniqueId = builder.uniqueId;
    }

    @Override
    public Text getDisplayName() {
        if (this.displayName == null) {
            final UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
            this.displayName = uss.get(this.uniqueId).map(NameUtil::getDisplayName).orElse(Text.of(getIdentifier()));
        }
        return this.displayName;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }
}
