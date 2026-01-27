package org.elkased.eventticketplatform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elkased.eventticketplatform.domain.entities.Event;
import org.elkased.eventticketplatform.domain.entities.EventStatusEnum;
import org.elkased.eventticketplatform.domain.entities.TicketType;
import org.elkased.eventticketplatform.domain.entities.User;
import org.elkased.eventticketplatform.repositories.EventRepository;
import org.elkased.eventticketplatform.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Bean
    @Profile("dev") // Only run in dev profile
    public CommandLineRunner loadData() {
        return args -> {
            // Check if data already exists
            if (eventRepository.count() > 10) {
                log.info("Data already exists. Skipping data loading.");
                return;
            }

            log.info("Loading dummy data...");

            // Create dummy organizers
            User organizer1 = createUser(
                    UUID.fromString("fad558ef-cd7e-4556-a7e6-96b30adc210c"),
                    "John Smith",
                    "john.smith@eventorganizer.com"
            );

            User organizer2 = createUser(
                    UUID.fromString("653b8b64-72a9-41a8-bc05-6252709f1414"),
                    "Sarah Johnson",
                    "sarah.johnson@eventorganizer.com"
            );

            User organizer3 = createUser(
                    UUID.fromString("eed09ee8-726f-4769-82a8-10964de182ff"),
                    "Michael Chen",
                    "michael.chen@eventorganizer.com"
            );

            userRepository.saveAll(List.of(organizer1, organizer2, organizer3));
            log.info("Created {} organizers", 3);

            // Create events with various ticket types
            List<Event> events = new ArrayList<>();

            // Event 1: Tech Conference - PUBLISHED
            events.add(createEvent(
                    "Tech Innovation Summit 2024",
                    "San Francisco Convention Center, CA",
                    LocalDateTime.now().plusDays(45),
                    LocalDateTime.now().plusDays(47),
                    LocalDateTime.now().minusDays(30),
                    LocalDateTime.now().plusDays(40),
                    EventStatusEnum.PUBLISHED,
                    organizer1,
                    List.of(
                            createTicketType("Early Bird", 299.99, "Limited early bird pricing - includes all sessions and workshops", 100),
                            createTicketType("General Admission", 499.99, "Full access to all conference sessions, workshops, and networking events", 500),
                            createTicketType("VIP Pass", 999.99, "Premium access with exclusive sessions, VIP lounge, and meet & greet with speakers", 50),
                            createTicketType("Student", 149.99, "Special student pricing - valid student ID required at check-in", 200)
                    )
            ));

            // Event 2: Music Festival - PUBLISHED
            events.add(createEvent(
                    "Summer Music Festival 2024",
                    "Central Park, New York, NY",
                    LocalDateTime.now().plusDays(60),
                    LocalDateTime.now().plusDays(62),
                    LocalDateTime.now().minusDays(20),
                    LocalDateTime.now().plusDays(55),
                    EventStatusEnum.PUBLISHED,
                    organizer2,
                    List.of(
                            createTicketType("Single Day Pass", 89.99, "Access to one day of the festival", 2000),
                            createTicketType("3-Day Pass", 199.99, "Full festival access for all three days", 1500),
                            createTicketType("VIP 3-Day Pass", 499.99, "Premium viewing areas, VIP lounge, and exclusive amenities", 300),
                            createTicketType("Camping Package", 349.99, "3-day pass plus camping spot for the weekend", 500)
                    )
            ));

            // Event 3: Food & Wine Expo - PUBLISHED
            events.add(createEvent(
                    "International Food & Wine Expo",
                    "Chicago Expo Center, IL",
                    LocalDateTime.now().plusDays(30),
                    LocalDateTime.now().plusDays(30).plusHours(8),
                    LocalDateTime.now().minusDays(15),
                    LocalDateTime.now().plusDays(28),
                    EventStatusEnum.PUBLISHED,
                    organizer1,
                    List.of(
                            createTicketType("General Admission", 75.00, "Access to all vendor booths and tastings", 800),
                            createTicketType("Premium Tasting", 150.00, "Includes general admission plus premium wine tasting sessions", 200),
                            createTicketType("Chef's Table Experience", 350.00, "Exclusive dining experience with celebrity chefs", 40),
                            createTicketType("Group Package (4 people)", 250.00, "General admission for 4 people - save $50!", 100)
                    )
            ));

            // Event 4: Sports Tournament - PUBLISHED
            events.add(createEvent(
                    "National Basketball Championship Finals",
                    "Madison Square Garden, New York, NY",
                    LocalDateTime.now().plusDays(20),
                    LocalDateTime.now().plusDays(20).plusHours(4),
                    LocalDateTime.now().minusDays(40),
                    LocalDateTime.now().plusDays(18),
                    EventStatusEnum.PUBLISHED,
                    organizer3,
                    List.of(
                            createTicketType("Upper Level", 125.00, "Upper level seating with great views", 1000),
                            createTicketType("Lower Level", 250.00, "Lower level seating closer to the action", 500),
                            createTicketType("Courtside", 1500.00, "Premium courtside seats - best in the house", 50),
                            createTicketType("Family Pack (4 tickets)", 400.00, "4 upper level tickets - perfect for families", 150)
                    )
            ));

            // Event 5: Art Exhibition - PUBLISHED
            events.add(createEvent(
                    "Modern Art Exhibition: Digital Dreams",
                    "Museum of Contemporary Art, Los Angeles, CA",
                    LocalDateTime.now().plusDays(10),
                    LocalDateTime.now().plusDays(40),
                    LocalDateTime.now().minusDays(5),
                    LocalDateTime.now().plusDays(35),
                    EventStatusEnum.PUBLISHED,
                    organizer2,
                    List.of(
                            createTicketType("Standard Entry", 25.00, "General admission to the exhibition", 500),
                            createTicketType("Guided Tour", 45.00, "Includes guided tour with art curator", 100),
                            createTicketType("Opening Night Gala", 200.00, "Exclusive opening night event with artists and champagne reception", 150),
                            createTicketType("Student/Senior", 15.00, "Discounted rate for students and seniors", 200)
                    )
            ));

            // Event 6: Comedy Show - PUBLISHED
            events.add(createEvent(
                    "Stand-Up Comedy Night: Laugh Out Loud",
                    "Comedy Club Downtown, Austin, TX",
                    LocalDateTime.now().plusDays(15),
                    LocalDateTime.now().plusDays(15).plusHours(3),
                    LocalDateTime.now().minusDays(10),
                    LocalDateTime.now().plusDays(14),
                    EventStatusEnum.PUBLISHED,
                    organizer3,
                    List.of(
                            createTicketType("General Seating", 35.00, "General admission seating", 200),
                            createTicketType("Premium Seating", 60.00, "Reserved seating in the front rows", 80),
                            createTicketType("VIP Table (4 people)", 300.00, "Private table for 4 with bottle service", 20),
                            createTicketType("Couples Package", 100.00, "2 premium seats plus complimentary drinks", 50)
                    )
            ));

            // Event 7: Business Workshop - DRAFT
            events.add(createEvent(
                    "Entrepreneurship Bootcamp 2024",
                    "Business Innovation Hub, Seattle, WA",
                    LocalDateTime.now().plusDays(90),
                    LocalDateTime.now().plusDays(92),
                    LocalDateTime.now().plusDays(5),
                    LocalDateTime.now().plusDays(85),
                    EventStatusEnum.DRAFT,
                    organizer1,
                    List.of(
                            createTicketType("Standard Pass", 599.00, "3-day intensive workshop with materials included", 100),
                            createTicketType("Premium Pass", 999.00, "Includes standard pass plus one-on-one mentoring sessions", 30),
                            createTicketType("Corporate Package (5 people)", 2500.00, "Team package for 5 employees", 20)
                    )
            ));

            // Event 8: Charity Run - PUBLISHED
            events.add(createEvent(
                    "5K Charity Run for Education",
                    "Riverside Park, Portland, OR",
                    LocalDateTime.now().plusDays(25),
                    LocalDateTime.now().plusDays(25).plusHours(4),
                    LocalDateTime.now().minusDays(20),
                    LocalDateTime.now().plusDays(23),
                    EventStatusEnum.PUBLISHED,
                    organizer2,
                    List.of(
                            createTicketType("Individual Runner", 30.00, "Registration for one runner - includes t-shirt and medal", 1000),
                            createTicketType("Family Registration (4 people)", 100.00, "Registration for family of 4", 200),
                            createTicketType("Virtual Participant", 20.00, "Participate remotely and receive digital certificate", 500),
                            createTicketType("Sponsor Runner", 100.00, "Runner registration plus donation to support education programs", 150)
                    )
            ));

            // Event 9: Gaming Convention - PUBLISHED
            events.add(createEvent(
                    "GameCon 2024: The Ultimate Gaming Experience",
                    "Las Vegas Convention Center, NV",
                    LocalDateTime.now().plusDays(70),
                    LocalDateTime.now().plusDays(73),
                    LocalDateTime.now().minusDays(25),
                    LocalDateTime.now().plusDays(65),
                    EventStatusEnum.PUBLISHED,
                    organizer3,
                    List.of(
                            createTicketType("Weekend Pass", 150.00, "4-day access to all gaming areas and tournaments", 3000),
                            createTicketType("Single Day", 50.00, "One day access", 2000),
                            createTicketType("Pro Gamer Pass", 400.00, "Includes weekend pass, tournament entry, and exclusive meet & greets", 200),
                            createTicketType("Exhibitor Pass", 250.00, "For vendors and exhibitors", 500)
                    )
            ));

            // Event 10: Film Festival - PUBLISHED
            events.add(createEvent(
                    "Independent Film Festival 2024",
                    "Historic Theater District, Boston, MA",
                    LocalDateTime.now().plusDays(50),
                    LocalDateTime.now().plusDays(57),
                    LocalDateTime.now().minusDays(30),
                    LocalDateTime.now().plusDays(45),
                    EventStatusEnum.PUBLISHED,
                    organizer1,
                    List.of(
                            createTicketType("All-Access Pass", 299.00, "Unlimited access to all screenings for 7 days", 300),
                            createTicketType("Single Screening", 15.00, "Ticket for one film screening", 2000),
                            createTicketType("Opening Night Premiere", 75.00, "Red carpet premiere with Q&A session", 200),
                            createTicketType("Filmmaker Pass", 500.00, "For filmmakers - includes networking events and workshops", 100)
                    )
            ));

            // Event 11: Yoga Retreat - DRAFT
            events.add(createEvent(
                    "Mindfulness & Yoga Retreat Weekend",
                    "Mountain Wellness Center, Denver, CO",
                    LocalDateTime.now().plusDays(100),
                    LocalDateTime.now().plusDays(102),
                    LocalDateTime.now().plusDays(10),
                    LocalDateTime.now().plusDays(95),
                    EventStatusEnum.DRAFT,
                    organizer2,
                    List.of(
                            createTicketType("Day Pass", 120.00, "Single day access to all yoga sessions and workshops", 50),
                            createTicketType("Weekend Package", 350.00, "Full weekend with accommodation and meals", 40),
                            createTicketType("Private Session Add-on", 100.00, "One-on-one session with master instructor", 20)
                    )
            ));

            // Event 12: Science Fair - PUBLISHED
            events.add(createEvent(
                    "National Science & Innovation Fair",
                    "Science Museum, Washington D.C.",
                    LocalDateTime.now().plusDays(35),
                    LocalDateTime.now().plusDays(37),
                    LocalDateTime.now().minusDays(15),
                    LocalDateTime.now().plusDays(33),
                    EventStatusEnum.PUBLISHED,
                    organizer3,
                    List.of(
                            createTicketType("Adult Admission", 20.00, "General admission for adults", 1000),
                            createTicketType("Child Admission", 10.00, "For children under 12", 1000),
                            createTicketType("Family Pass", 50.00, "2 adults and up to 3 children", 300),
                            createTicketType("School Group (20 students)", 150.00, "Discounted rate for school groups", 50)
                    )
            ));

            eventRepository.saveAll(events);
            log.info("Created {} events with various ticket types", events.size());
            log.info("Dummy data loading completed successfully!");
        };
    }

    private User createUser(UUID id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        return user;
    }

    private Event createEvent(String name, String venue, LocalDateTime start, LocalDateTime end,
                              LocalDateTime salesStart, LocalDateTime salesEnd,
                              EventStatusEnum status, User organizer,
                              List<TicketType> ticketTypes) {
        Event event = Event.builder()
                .name(name)
                .venue(venue)
                .start(start)
                .end(end)
                .salesStart(salesStart)
                .salesEnd(salesEnd)
                .status(status)
                .organizer(organizer)
                .createdAt(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build();

        // Set the event reference for each ticket type
        ticketTypes.forEach(ticketType -> {
            ticketType.setEvent(event);
            ticketType.setCreatedAt(LocalDateTime.now());
            ticketType.setUpdated(LocalDateTime.now());
        });

        event.setTicketTypes(ticketTypes);
        return event;
    }

    private TicketType createTicketType(String name, Double price, String description, Integer totalAvailable) {
        return TicketType.builder()
                .name(name)
                .price(price)
                .description(description)
                .totalAvailable(totalAvailable)
                .build();
    }
}
