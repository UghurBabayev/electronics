package az.electronika.demo.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int  MAX_ATTEMPTS   = 10;
    private static final long BLOCK_DURATION = 15 * 60; // saniyə

    private record Attempts(int count, Instant blockedUntil) {}

    private final ConcurrentHashMap<String, Attempts> cache = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempts a = cache.get(key);
        if (a == null) return false;
        if (a.blockedUntil() != null && Instant.now().isBefore(a.blockedUntil())) return true;
        if (a.blockedUntil() != null) cache.remove(key); // blok bitti, təmizlə
        return false;
    }

    public void loginFailed(String key) {
        Attempts current = cache.getOrDefault(key, new Attempts(0, null));
        int newCount = current.count() + 1;
        Instant block = newCount >= MAX_ATTEMPTS
                ? Instant.now().plusSeconds(BLOCK_DURATION)
                : null;
        cache.put(key, new Attempts(newCount, block));
    }

    public void loginSucceeded(String key) {
        cache.remove(key);
    }

    public long secondsRemaining(String key) {
        Attempts a = cache.get(key);
        if (a == null || a.blockedUntil() == null) return 0;
        return Math.max(0, a.blockedUntil().getEpochSecond() - Instant.now().getEpochSecond());
    }
}