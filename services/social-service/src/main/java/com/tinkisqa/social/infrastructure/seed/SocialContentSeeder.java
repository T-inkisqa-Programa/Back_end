package com.tinkisqa.social.infrastructure.seed;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.tinkisqa.social.domain.model.Community;
import com.tinkisqa.social.domain.model.CommunityPrivacy;
import com.tinkisqa.social.domain.model.Post;
import com.tinkisqa.social.domain.model.PostComment;
import com.tinkisqa.social.domain.model.PostPrivacy;
import com.tinkisqa.social.domain.port.CommunityRepository;
import com.tinkisqa.social.domain.port.PostCommentRepository;
import com.tinkisqa.social.domain.port.PostRepository;

/**
 * Contenido de demostración: las comunidades de plataforma y el muro inicial.
 *
 * <p>Solo se ejecuta si {@code tinkisqa.social.seed.enabled=true} (por ejemplo
 * desde {@code docker-compose.yml}); está desactivado por defecto y nunca debe
 * activarse en producción. Es idempotente: usa identificadores fijos y, si la
 * primera comunidad ya existe, no vuelve a sembrar.</p>
 *
 * <p>Las comunidades de plataforma no tienen dueña ({@code ownerId} nulo): las
 * modera la administración global. Las autoras del muro son identidades de
 * demostración que no existen en el IAM; el contenido guarda su nombre y avatar
 * como instantánea.</p>
 */
@Component
@ConditionalOnProperty(prefix = "tinkisqa.social.seed", name = "enabled", havingValue = "true")
public class SocialContentSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SocialContentSeeder.class);

    private static final UUID FIRST_COMMUNITY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private record SeedCommunity(UUID id, String name, String description, String image, String category) {
    }

    private record SeedPost(
            UUID id,
            UUID authorId,
            String author,
            String avatar,
            int minutesAgo,
            String tag,
            String content,
            boolean quote,
            String image) {
    }

    private static final List<SeedCommunity> COMMUNITIES = List.of(
            new SeedCommunity(communityId(1), "Adolescentes",
                    "Un espacio seguro para compartir dudas, sueños y experiencias entre chicas.",
                    "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=900&q=80",
                    "Adolescencia"),
            new SeedCommunity(communityId(2), "Familia completa",
                    "Conecta con otras mujeres que buscan armonía y bienestar en sus hogares.",
                    "https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&w=900&q=80",
                    "Familia"),
            new SeedCommunity(communityId(3), "Autoestima",
                    "Fortalece tu amor propio y comparte herramientas para brillar desde dentro.",
                    "https://images.unsplash.com/photo-1513364776144-60967b0f800f?auto=format&fit=crop&w=900&q=80",
                    "Bienestar"),
            new SeedCommunity(communityId(4), "Carreras",
                    "Inspírate con historias de mujeres profesionales y construye tu camino laboral.",
                    "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?auto=format&fit=crop&w=900&q=80",
                    "Trabajo"),
            new SeedCommunity(communityId(5), "Maternidad",
                    "Comparte experiencias, dudas y alegrías del camino de ser madre.",
                    "https://images.unsplash.com/photo-1522338242992-e1a54906a8da?auto=format&fit=crop&w=900&q=80",
                    "Maternidad"),
            new SeedCommunity(communityId(6), "Emprendimiento",
                    "Impulsa tus ideas de negocio con el apoyo de otras mujeres emprendedoras.",
                    "https://images.unsplash.com/photo-1556740738-b6a63e27c4df?auto=format&fit=crop&w=900&q=80",
                    "Emprendimiento"));

    private static final String VALERIA_AVATAR =
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=100&q=80";
    private static final String CAMILA_AVATAR =
            "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=100&q=80";
    private static final String SOFIA_AVATAR =
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80";

    private static final List<SeedPost> POSTS = List.of(
            new SeedPost(postId(1), demoAuthor(1), "Valeria M.", VALERIA_AVATAR, 120,
                    "Idea Compartida",
                    "Hoy en la escuela una compañera me dijo que le gustaría aprender más sobre cómo manejar "
                            + "el estrés antes de los exámenes. ¿A alguien más le pasa? Me encantaría que "
                            + "compartamos tips 💪",
                    false, null),
            new SeedPost(postId(2), demoAuthor(2), "Camila R.", CAMILA_AVATAR, 1440,
                    null,
                    "\"No tienes que ser perfecta para ser increíble. Cada pequeño paso que das hacia tus "
                            + "sueños ya te hace más fuerte de lo que imaginas.\" ✨",
                    true, null),
            new SeedPost(postId(3), demoAuthor(3), "Sofía G.", SOFIA_AVATAR, 4320,
                    "Idea Compartida",
                    "Ayer me animé a hablar en la clase sobre mi proyecto de ciencia y recibí mucho apoyo. "
                            + "Si están dudando en alzar la voz, háganlo. Su opinión importa 💜",
                    false, null),
            new SeedPost(postId(4), demoAuthor(1), "Valeria M.", VALERIA_AVATAR, 7200,
                    "Pregunta",
                    "¿Alguien ha probado técnicas de respiración para calmar los nervios? Estoy buscando "
                            + "nuevas herramientas para usarlas antes de hablar en público.",
                    false, null));

    private final CommunityRepository communities;
    private final PostRepository posts;
    private final PostCommentRepository comments;
    private final Clock clock;

    public SocialContentSeeder(
            CommunityRepository communities,
            PostRepository posts,
            PostCommentRepository comments,
            Clock clock) {
        this.communities = communities;
        this.posts = posts;
        this.comments = comments;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (communities.findById(FIRST_COMMUNITY_ID).isPresent()) {
            log.info("Contenido social de demostración ya presente; no se vuelve a sembrar.");
            return;
        }

        Instant now = clock.instant();
        COMMUNITIES.forEach(community -> communities.save(new Community(
                community.id(), community.name(), community.description(), community.image(),
                community.category(), CommunityPrivacy.PUBLICA, null, now, now)));

        for (SeedPost seed : POSTS) {
            Instant createdAt = now.minus(Duration.ofMinutes(seed.minutesAgo()));
            persist(seed, createdAt);
        }

        seedComment(postId(1), demoAuthor(3), "Sofía G.", SOFIA_AVATAR, 90,
                "¡Qué buena idea! A mí me ayuda escribir lo que siento antes de estudiar. 💜");
        seedComment(postId(1), demoAuthor(2), "Camila R.", CAMILA_AVATAR, 60,
                "A mí me funciona la respiración 4-7-8. La practico antes de los exámenes.");

        log.info("Contenido de demostración creado: {} comunidades y {} publicaciones.",
                COMMUNITIES.size(), POSTS.size());
    }

    private void persist(SeedPost seed, Instant createdAt) {
        Post post = Post.published(
                seed.id(),
                seed.authorId(),
                seed.author(),
                seed.avatar(),
                null,
                seed.tag(),
                seed.content(),
                seed.quote(),
                seed.image(),
                null, null, null, null,
                null, null, null,
                Set.of(),
                PostPrivacy.PUBLICO,
                createdAt);
        posts.save(post);
    }

    private void seedComment(UUID postId, UUID authorId, String author, String avatar, int minutesAgo, String text) {
        Instant now = clock.instant();
        comments.save(PostComment.added(
                UUID.randomUUID(),
                postId,
                authorId,
                author,
                avatar,
                text,
                now.minus(Duration.ofMinutes(minutesAgo))));
    }

    private static UUID communityId(int index) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(index));
    }

    private static UUID postId(int index) {
        return UUID.fromString("10000000-0000-0000-0000-%012d".formatted(index));
    }

    private static UUID demoAuthor(int index) {
        return UUID.fromString("20000000-0000-0000-0000-%012d".formatted(index));
    }
}
