package com.example.school.participants;

import com.example.school.event.Event;
import com.example.school.event.EventRep;
import com.example.school.participants.assignedparticipants.AssignedParticipant;
import com.example.school.participants.assignedparticipants.AssignedRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itextpdf.text.Document;
import com.itextpdf.text.*;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRep participantRep;

    @Autowired
    private EventRep eventRep;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private AssignedRepo assignedRepo;


    public Participants save(RequestDto data, MultipartFile photo, Long eventId) throws IOException {
        ObjectMapper mapper = new ObjectMapper(); // it from jackson library in spirng boot to handle jso
        // Json to javaObject
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        if (participantRep.existsByParticipantNameIgnoreCaseAndDeletedFalse(data.getParticipantName())) {
            throw new RuntimeException(
                    "Participant '" + data.getParticipantName() + "' already exists!"
            );
        }

        Participants participants = new Participants();

        // populate fields
        participants.setParticipantName(data.getParticipantName());
        participants.setDateOfBirth(data.getDateOfBirth());
        participants.setAge(data.getAge());
        participants.setEventId(eventId);

        participants.setGender(data.getGender());
        participants.setCategory(data.getCategory());
        participants.setSchoolName(data.getSchoolName());
        participants.setStandard(data.getGroup());
        participants.setStandard(data.getStandard());
        participants.setYogaMasterName(data.getYogaMasterName());
        participants.setYogaMasterContact(data.getYogaMasterContact());
        participants.setAddress(data.getAddress());
        participants.setStatus("Requested");


        Optional<Participants> lastProduct = participantRep.findTopByOrderByIdDesc();

        String newId = "MEM0001"; // Default if no previous ID exists

        if (lastProduct.isPresent()) {
            String lastId = lastProduct.get().getParticipantCode();

            if (lastId != null && !lastId.isEmpty()) {
                // Extract number part after prefix "MEM"
                int lastNumber = Integer.parseInt(lastId.replace("MEM", ""));
                newId = String.format("MEM%04d", lastNumber + 1); // Increment and format
            }
        }

        participants.setParticipantCode(newId);

        // ✅ Handle file saving (optional)
        if (photo != null && !photo.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File directory = new File(uploadDir);
            if (!directory.exists())
                directory.mkdirs();

            String fileName = photo.getOriginalFilename();

            String filePath = uploadDir + "/" + fileName; // fixed path handling
            photo.transferTo(new File(filePath));
            participants.setPhoto(fileName);
        } else {
            participants.setPhoto(null);
        }

        return participantRep.save(participants);


    }

    @Transactional
    public Participants updateStatus(Long id, String status) {
        Participants participant = participantRep.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setStatus(status);
        return participantRep.save(participant); // save and return updated entity
    }
    public Page<ParticipantsDto> getFilteredByEvent(Long eventId, PageFilterRequest request) {

        Pageable pageable = request.toPageable();
        ParticipantFilter filter = request.getFilter();

        String filterParticipant = filter != null ? filter.getParticipant() : null;
        String filterStatus = filter != null && filter.getStatus() != null ? filter.getStatus().toUpperCase() : null;
        String filterGroup = filter != null ? filter.getGroup() : null;
        String filterCategory = filter != null ? filter.getCategory() : null;
        String filterAssignmentStatus = filter != null ? filter.getAssignmentStatus() : null;

        // 1️⃣ Fetch participants from DB WITHOUT category filter (multi-category handled in Java)
        Page<Participants> participants = participantRep.findFilteredParticipants(
                filterParticipant,
                filterStatus,
                null,
                filterGroup,
                eventId,
                pageable
        );

        // 2️⃣ Fetch all assigned participants for this event
        List<AssignedParticipant> assignedList = assignedRepo.findAllByEventId(eventId);

        // 3️⃣ Build DTOs with category & assignmentStatus logic
        List<ParticipantsDto> responseList = participants.stream()
                .map(p -> {
                    ParticipantsDto dto = new ParticipantsDto(p);

                    // Split participant's category
                    List<String> participantCategories = Arrays.stream(p.getCategory().split(","))
                            .map(String::trim)
                            .toList();

                    // If filterCategory is provided, only show that category
                    if (filterCategory != null) {
                        dto.setCategory(filterCategory);

                        boolean assigned = assignedList.stream().anyMatch(a ->
                                a.getParticipantId().equals(p.getId()) &&
                                        a.getCategory().equalsIgnoreCase(filterCategory)
                        );

                        dto.setAssignmentStatus(assigned ? "Assigned" : "Un Assigned");
                    } else {
                        // If no category filter, just keep full category and assignmentStatus as "Assigned"/"Un Assigned" per category
                        // For simplicity, here we mark "Assigned" if assigned in any category
                        boolean assigned = assignedList.stream().anyMatch(a ->
                                a.getParticipantId().equals(p.getId())
                        );
                        dto.setAssignmentStatus(assigned ? "Assigned" : "Un Assigned");
                    }

                    return dto;
                })
                // 4️⃣ Apply assignmentStatus filter if provided
                .filter(dto -> {
                    if (filterAssignmentStatus != null) {
                        return dto.getAssignmentStatus().equalsIgnoreCase(filterAssignmentStatus);
                    }
                    return true;
                })
                .toList();

        return new PageImpl<>(responseList, pageable, responseList.size());
    }

//    public Page<ParticipantsDto> getFilteredByEvent(Long eventId, PageFilterRequest request) {
//        Pageable pageable = request.toPageable();
//        ParticipantFilter filter = request.getFilter();
//
//        String participantValue = filter != null ? filter.getParticipant() : null;
//        String groupValue = filter != null ? filter.getGroup() : null;
//        String statusFilter = filter != null && filter.getStatus() != null
//                ? filter.getStatus().toUpperCase()
//                : null;
//        // Fetch participants based on other filters
//        Page<Participants> participants = participantRep.findFilteredParticipants(
//                participantValue,
//                statusFilter,
//                filter != null ? filter.getCategory() : null,
//                groupValue,
//                eventId,
//                pageable
//        );
//
//        // Get assigned IDs
//        List<Long> assignedIds = (filter != null && filter.getCategory() != null) ?
//                assignedRepo.findAllByEventIdAndCategory(eventId, filter.getCategory())
//                        .stream().map(AssignedParticipant::getParticipantId).toList() :
//                assignedRepo.findAllByEventId(eventId)
//                        .stream().map(AssignedParticipant::getParticipantId).toList();
//
//        // Map to DTOs with assignmentStatus
//        List<ParticipantsDto> responseList = participants.stream()
//                .map(p -> {
//                    ParticipantsDto res = new ParticipantsDto(p);
//                    res.setAssignmentStatus(assignedIds.contains(p.getId()) ? "Assigned" : "Not Assigned");
//                    return res;
//                })
//                // Apply assignmentStatus filter if provided
//                .filter(p -> {
//                    if (filter != null && filter.getAssignmentStatus() != null) {
//                        return p.getAssignmentStatus().equalsIgnoreCase(filter.getAssignmentStatus());
//                    }
//                    return true; // if no filter, keep all
//                })
//                .toList();
//
//        return new PageImpl<>(responseList, pageable, responseList.size());
//    }


    public Participants getById(Long id) {
        return participantRep.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant not found with id: " + id));
    }

    @Transactional
    public Participants update(RequestDto data, MultipartFile file, Long id) throws IOException {
        if (participantRep.existsByParticipantNameIgnoreCaseAndIdNotAndDeletedFalse(
                data.getParticipantName(),
                data.getId()
        )) {
            throw new RuntimeException("Another participant with the same name already exists!");
        }

        Participants participants = participantRep.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant not found!"));

        participants.setParticipantName(data.getParticipantName());
        participants.setDateOfBirth(data.getDateOfBirth());
        participants.setAge(data.getAge());
        //participants.setEventId(eventId);

        participants.setGender(data.getGender());
        participants.setCategory(data.getCategory());
        participants.setSchoolName(data.getSchoolName());
        participants.setStandard(data.getStandard());
        participants.setYogaMasterName(data.getYogaMasterName());
        participants.setYogaMasterContact(data.getYogaMasterContact());
        participants.setAddress(data.getAddress());
        participants.setStandard(data.getGroup());
        //participants.setStatus("Requested");
        // File upload → EXACT same style as Event save()
        if (file != null && !file.isEmpty()) {

            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File directory = new File(uploadDir);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = file.getOriginalFilename();
            String filePath = uploadDir + "/" + fileName;

            file.transferTo(new File(filePath));

            participants.setPhoto(fileName);
        } else {
            participants.setPhoto(null);
        }

        return participantRep.save(participants);
    }

    public byte[] generateParticipantPdf(Long id) {
        Participants p = participantRep.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        return generateParticipantPDF(p);
    }
    public byte[] generateParticipantPDF(Participants p) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // --------- Fonts ----------
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font valueFont = new Font(Font.FontFamily.HELVETICA, 12);
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);

            // --------- Header ---------
            Paragraph header = new Paragraph("Registration Receipt", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(5);
            document.add(header);

            Paragraph subHeader = new Paragraph("~ International Yoga Festival ~", subHeaderFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(20);
            document.add(subHeader);

            document.add(new Paragraph("------------------------------------------------------------"));

            // --------- Participant Details ---------
            document.add(makeLine("Participant Name", p.getParticipantName(), labelFont, valueFont));
            document.add(makeLine("Participant Code", p.getParticipantCode(), labelFont, valueFont));
            document.add(makeLine("Event ID", String.valueOf(p.getEventId()), labelFont, valueFont));
            document.add(makeLine("Gender", p.getGender(), labelFont, valueFont));
            document.add(makeLine("Age", String.valueOf(p.getAge()), labelFont, valueFont));
            document.add(makeLine("Category", p.getCategory(), labelFont, valueFont));
            document.add(makeLine("School Name", p.getSchoolName(), labelFont, valueFont));
            document.add(makeLine("Standard", p.getStandard(), labelFont, valueFont));
            document.add(makeLine("Yoga Master", p.getYogaMasterName(), labelFont, valueFont));
            document.add(makeLine("Contact No.", String.valueOf(p.getYogaMasterContact()), labelFont, valueFont));
            document.add(makeLine("Address", p.getAddress(), labelFont, valueFont));

            document.add(new Paragraph("------------------------------------------------------------"));

            // -------- STATUS BADGE ----------
            PdfPTable badgeTable = new PdfPTable(1);
            badgeTable.setWidthPercentage(30);
            badgeTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell badgeCell = new PdfPCell(new Phrase("STATUS: " + "Not Paid",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            badgeCell.setPadding(8);






            badgeTable.addCell(badgeCell);
            document.add(badgeTable);

            document.add(new Paragraph(" "));

            // --------- Authorized Seal on Right ----------
            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setWidthPercentage(30);
            footerTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            PdfPCell sealCell = new PdfPCell(new Phrase("Authorized Sign", labelFont));
            sealCell.setBorder(Rectangle.NO_BORDER);
            sealCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footerTable.addCell(sealCell);

            document.add(footerTable);

            // -------- Thank You Text --------
            Paragraph footerText = new Paragraph("Thank you for joining the journey of wellness.", footerFont);
            footerText.setAlignment(Element.ALIGN_CENTER);
            footerText.setSpacingBefore(20);
            document.add(footerText);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private Paragraph makeLine(String label, String value, Font labelFont, Font valueFont) {
        Paragraph line = new Paragraph();
        line.add(new Chunk(label + ": ", labelFont));
        line.add(new Chunk(value != null ? value : "—", valueFont));
        line.setSpacingBefore(8);
        return line;
    }


    public Participants getParticipantById(Long id) {
        return participantRep.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant not found with id: " + id));
    }
//    public byte[] generateCertificatePdf(Long participantId) throws Exception {
//
//        // 1️⃣ Fetch participant details
//        Participants participant = getParticipantById(participantId);
//        if (participant == null) {
//            throw new RuntimeException("Participant not found");
//        }
//
//        // 2️⃣ Prepare Thymeleaf context
//        Context context = new Context();
//        context.setVariable("participantName", participant.getParticipantName());
//        context.setVariable("coordinator", "Jane Smith");
//        context.setVariable("schoolName",participant.getSchoolName());
//        context.setVariable("message", "Your dedication and commitment to the art of yoga is commendable. We celebrate your journey of wellness and growth.");
//
//        // 3️⃣ Render Thymeleaf HTML template
//        String htmlContent = templateEngine.process("certificate", context);
//
//        // 4️⃣ Convert HTML to PDF (iText 5)
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
//        PdfWriter.getInstance(document, out);
//        document.open();
//
//        List<Element> elements = HTMLWorker.parseToList(new StringReader(htmlContent), null);
//        for (Element e : elements) {
//            document.add(e);
//        }
//
//        document.close();
//        return out.toByteArray();
//    }
public byte[] generateCertificatePdf(Long participantId) throws IOException {
    Participants participant = getParticipantById(participantId);

    Context context = new Context();
    context.setVariable("participantName", participant.getParticipantName());
    context.setVariable("coordinator", "Jane Smith");
    context.setVariable("schoolName", participant.getSchoolName());
    context.setVariable("message", "Your dedication and commitment to yoga is commendable.");

    String htmlContent = templateEngine.process("certificate", context);

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(htmlContent, null);
        // Optionally register fonts
        // builder.useFont(() -> getClass().getResourceAsStream("/fonts/Georgia.ttf"), "Georgia");
        builder.toStream(outputStream);
        builder.run();

        return outputStream.toByteArray();
    } catch (Exception e) {
        throw new IOException("Error generating PDF", e);
    }
}

}



