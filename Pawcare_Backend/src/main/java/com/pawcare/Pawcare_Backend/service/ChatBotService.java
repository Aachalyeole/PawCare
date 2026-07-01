package com.pawcare.Pawcare_Backend.service;

import com.pawcare.Pawcare_Backend.dto.ChatResponse;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatBotService {

    private Map<String, String> responses;

    public ChatBotService() {
        initializeResponses();
    }

    private void initializeResponses() {
        responses = new HashMap<>();

        // Emergency responses
        responses.put("emergency|urgent|critical|serious",
                "⚠️ EMERGENCY: If animal is critically injured or bleeding heavily:\n" +
                        "1. Call emergency vet immediately\n" +
                        "2. Keep animal warm and calm\n" +
                        "3. Don't move if spinal injury suspected\n" +
                        "4. Control bleeding with clean cloth\n" +
                        "📞 Emergency Vet Helpline: 1800-XXX-XXXX");

        responses.put("bite|bitten|attack",
                "🐕 Animal Bite First Aid:\n" +
                        "1. Wash wound with soap and water for 5 minutes\n" +
                        "2. Apply antiseptic cream\n" +
                        "3. Seek medical help immediately\n" +
                        "4. Observe animal for rabies symptoms\n" +
                        "⚠️ Visit doctor for rabies vaccine if needed");

        responses.put("bleeding|blood|hemorrhage",
                "🩸 Bleeding First Aid:\n" +
                        "1. Apply direct pressure with clean cloth\n" +
                        "2. Elevate wound if possible\n" +
                        "3. Don't remove embedded objects\n" +
                        "4. Apply tourniquet ONLY for severe bleeding\n" +
                        "5. Rush to vet immediately");

        responses.put("broken bone|fracture|limb",
                "🦴 Broken Bone Emergency:\n" +
                        "1. Don't try to straighten the bone\n" +
                        "2. Immobilize the area with splint\n" +
                        "3. Support limb while transporting\n" +
                        "4. Keep animal calm and warm\n" +
                        "5. Rush to vet immediately");

        // First aid responses
        responses.put("first aid|emergency care",
                "🚑 Basic Animal First Aid:\n" +
                        "• Keep a first aid kit ready\n" +
                        "• Learn CPR for animals\n" +
                        "• Know nearest emergency vet\n" +
                        "• Keep emergency numbers handy\n" +
                        "• Don't panic - stay calm");

        responses.put("cpr|resuscitation",
                "❤️ Animal CPR Steps:\n" +
                        "1. Check responsiveness\n" +
                        "2. Open airway (pull tongue forward)\n" +
                        "3. Give 2 rescue breaths\n" +
                        "4. 30 chest compressions\n" +
                        "5. Repeat until breathing starts\n" +
                        "⚠️ Only perform if trained!");

        responses.put("choking|swallowed",
                "🫁 Choking First Aid:\n" +
                        "1. Look inside mouth - remove visible object\n" +
                        "2. Don't blind sweep (may push deeper)\n" +
                        "3. Perform Heimlich maneuver (if trained)\n" +
                        "4. Rush to vet if can't remove");

        // Poisoning responses
        responses.put("poison|toxic|poisoning",
                "☠️ Poisoning Emergency:\n" +
                        "1. Identify what was consumed\n" +
                        "2. Call vet immediately\n" +
                        "3. Don't induce vomiting without vet advice\n" +
                        "4. Keep sample of poison if possible\n" +
                        "5. Rush to emergency vet\n" +
                        "⚠️ Common toxins: chocolate, grapes, onions, rat poison");

        responses.put("chocolate|grape|onion|toxic food",
                "🍫 Food Poisoning Alert!\n" +
                        "Toxic foods for animals:\n" +
                        "• Chocolate (all types)\n" +
                        "• Grapes & Raisins\n" +
                        "• Onions & Garlic\n" +
                        "• Xylitol (sweetener)\n" +
                        "• Avocado\n" +
                        "• Macadamia nuts\n" +
                        "⚠️ Contact vet immediately if consumed!");

        // General care responses
        responses.put("care|pet care|animal care",
                "🐾 General Animal Care Tips:\n" +
                        "• Regular vet checkups\n" +
                        "• Vaccinations on schedule\n" +
                        "• Proper nutrition\n" +
                        "• Clean water always available\n" +
                        "• Daily exercise\n" +
                        "• Grooming and hygiene\n" +
                        "• Love and attention ❤️");

        responses.put("food|diet|feeding",
                "🍖 Proper Animal Nutrition:\n" +
                        "• Species-appropriate food\n" +
                        "• Age-appropriate diet\n" +
                        "• No human junk food\n" +
                        "• Fresh water always\n" +
                        "• Regular feeding schedule\n" +
                        "• Consult vet for special diets");

        responses.put("vaccination|vaccine|shots",
                "💉 Vaccination Schedule:\n" +
                        "• Puppies/Kittens: 6-8 weeks (first shot)\n" +
                        "• Booster: every 3-4 weeks until 16 weeks\n" +
                        "• Adults: Annual boosters\n" +
                        "• Rabies vaccine: Mandatory by law\n" +
                        "• Keep vaccination records updated");

        responses.put("deworming|worms|parasite",
                "🐛 Deworming Schedule:\n" +
                        "• Puppies: every 2 weeks until 12 weeks\n" +
                        "• Then monthly until 6 months\n" +
                        "• Adults: every 3-6 months\n" +
                        "• Signs: weight loss, bloated belly, worms in stool\n" +
                        "• Consult vet for proper medication");

        responses.put("grooming|bath|clean",
                "🧼 Grooming Tips:\n" +
                        "• Brush regularly (daily for long hair)\n" +
                        "• Bathe every 2-3 months\n" +
                        "• Clean ears weekly\n" +
                        "• Trim nails monthly\n" +
                        "• Brush teeth daily/weekly\n" +
                        "• Check for ticks/fleas");

        // Lost and found
        responses.put("lost|missing|stray",
                "🔍 Found a Stray/Lost Animal:\n" +
                        "1. Check for ID tag/collar\n" +
                        "2. Scan for microchip at vet\n" +
                        "3. Post on social media\n" +
                        "4. Notify local shelters\n" +
                        "5. Put up 'Found' posters\n" +
                        "6. Provide food and water\n" +
                        "7. Temporary shelter if needed");

        responses.put("adopt|adoption|rescue",
                "🏠 Animal Adoption Guide:\n" +
                        "• Research before adopting\n" +
                        "• Prepare home environment\n" +
                        "• Visit local shelters\n" +
                        "• Ask about animal's history\n" +
                        "• Budget for vet care\n" +
                        "• Commit for lifetime\n" +
                        "• Adopt, don't shop! ❤️");

        // Heat stroke and weather
        responses.put("heat stroke|hot|summer",
                "☀️ Heat Stroke Prevention:\n" +
                        "• NEVER leave in parked car\n" +
                        "• Provide shade and water\n" +
                        "• Walk during cool hours\n" +
                        "• Watch for excessive panting\n" +
                        "• Signs: drooling, weakness, collapse\n" +
                        "• Emergency: cool with wet towels, rush to vet");

        responses.put("cold|winter|hypothermia",
                "❄️ Cold Weather Care:\n" +
                        "• Bring pets indoors\n" +
                        "• Provide warm bedding\n" +
                        "• Never leave in cold car\n" +
                        "• Check paws for ice/salt\n" +
                        "• Use pet-safe antifreeze\n" +
                        "• Watch for shivering/lethargy");

        // Default response
        responses.put("default",
                "🐾 I'm here to help animals!\n\n" +
                        "I can provide information about:\n" +
                        "• Emergency first aid 🚑\n" +
                        "• Animal care tips 🐕\n" +
                        "• Poisoning emergencies ☠️\n" +
                        "• Lost/found animals 🔍\n" +
                        "• Adoption guidance 🏠\n" +
                        "• Vaccination schedules 💉\n\n" +
                        "Just type your question or concern!\n" +
                        "For emergencies, call vet immediately 📞");
    }

    public String getResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Please type your question about animal care! 🐾";
        }

        String lowerMessage = message.toLowerCase().trim();

        // Check for emergency keywords first
        if (containsEmergencyKeyword(lowerMessage)) {
            return getEmergencyResponse();
        }

        // Check against response patterns
        for (Map.Entry<String, String> entry : responses.entrySet()) {
            String pattern = entry.getKey();
            if (matchesPattern(lowerMessage, pattern)) {
                return entry.getValue();
            }
        }

        return responses.get("default");
    }

    private boolean containsEmergencyKeyword(String message) {
        String[] emergencyKeywords = {"emergency", "urgent", "critical", "911", "help", "dying", "severe"};
        for (String keyword : emergencyKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String getEmergencyResponse() {
        return "🚨 URGENT EMERGENCY DETECTED! 🚨\n\n" +
                "⚠️ IMMEDIATE ACTIONS:\n" +
                "1. Call emergency vet immediately\n" +
                "2. Keep animal calm and warm\n" +
                "3. Don't move if spinal injury suspected\n" +
                "4. Control bleeding with clean cloth\n\n" +
                "📞 Emergency Vet Helpline: 1800-XXX-XXXX\n" +
                "📍 Nearest Animal Hospital: [Your location]\n\n" +
                "Type 'first aid' for emergency care instructions";
    }

    private boolean matchesPattern(String message, String pattern) {
        String[] keywords = pattern.split("\\|");
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // Advanced response with category
    public ChatResponse getChatResponse(String message) {
        String reply = getResponse(message);
        String category = determineCategory(message);
        return new ChatResponse(reply, category);
    }

    private String determineCategory(String message) {
        String lowerMsg = message.toLowerCase();
        if (lowerMsg.matches(".*(emergency|urgent|bleeding|bite|broken|poison|choking).*")) {
            return "emergency";
        } else if (lowerMsg.matches(".*(care|food|vaccination|grooming|deworming).*")) {
            return "care";
        } else if (lowerMsg.matches(".*(lost|stray|adopt|rescue).*")) {
            return "rescue";
        }
        return "general";
    }
}