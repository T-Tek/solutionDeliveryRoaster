package com.solutiondeliveryroaster.solutiondelivery.service;

import com.solutiondeliveryroaster.solutiondelivery.DTO.TeamRequestDTO;
import com.solutiondeliveryroaster.solutiondelivery.DTO.TeamResponseDTO;
import com.solutiondeliveryroaster.solutiondelivery.entity.Team;
import com.solutiondeliveryroaster.solutiondelivery.exceptions.DuplicateTeamMemberException;
import com.solutiondeliveryroaster.solutiondelivery.exceptions.DuplicateTeamNameException;
import com.solutiondeliveryroaster.solutiondelivery.exceptions.ResourceNotFoundException;
import com.solutiondeliveryroaster.solutiondelivery.repository.TeamRepository;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.*;
import java.util.*;

@Service
public class TeamServiceImpl implements TeamService {

    //xkxpafoizgstwbaw

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamServiceImpl.class);
    private final TeamRepository teamRepository;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }
    @Override
    public List<TeamResponseDTO> getAllTeams() {
        LOGGER.info("Retrieving all teams...");
        List<Team> teams = teamRepository.findAll();

        LOGGER.debug("Total teams retrieved: {}", teams.size());
        List<TeamResponseDTO> teamResponseDTOList = new ArrayList<>();

        for (Team team : teams) {
            TeamResponseDTO teamResponseDTO = new TeamResponseDTO(
                    team.getId(),
                    team.getTeamName(),
                    team.getTeamMembers()
            );
            teamResponseDTOList.add(teamResponseDTO);
        }
        return teamResponseDTOList;
    }


    @Override
    public TeamResponseDTO getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return new TeamResponseDTO(
                team.getId(),
                team.getTeamName(),
                team.getTeamMembers()
        );
    }

    @Override
    public TeamResponseDTO createTeam(@Valid TeamRequestDTO requestDTO) {
        String teamName = requestDTO.getTeamName();
        LOGGER.info("Creating a Team...");
        Set<String> teamMembers = new HashSet<>(requestDTO.getTeamMembers());

        if (isTeamNameExists(teamName)) {
            throw new DuplicateTeamNameException("Team name already exists.");
        }
        if (areTeamMembersExistsInOtherTeams(teamMembers)) {
            throw new DuplicateTeamMemberException("One or more team members already exist in another team.");
        }

        Team team = Team.builder()
                .teamName(requestDTO.getTeamName())
                .teamMembers(new HashSet<>(requestDTO.getTeamMembers()))
                .build();

        Team createdTeam = teamRepository.save(team);
        return new TeamResponseDTO(
                createdTeam.getId(),
                createdTeam.getTeamName(),
                createdTeam.getTeamMembers()
        );
    }

    @Override
    public TeamResponseDTO addTeamMemberToTeam(Long teamId, String teamMember) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        if (isTeamMemberExistsInOtherTeams(teamMember)) {
            throw new DuplicateTeamMemberException("Team member already exists in another team.");
        }

        team.getTeamMembers().add(teamMember);
        Team updatedTeam = teamRepository.save(team);

        return new TeamResponseDTO(
                updatedTeam.getId(),
                updatedTeam.getTeamName(),
                new HashSet<>(updatedTeam.getTeamMembers())
        );
    }

    @Override
    public TeamResponseDTO updateTeam(Long id, TeamRequestDTO requestDTO) {
        Team team = Team.builder()
                .id(id)
                .teamName(requestDTO.getTeamName())
                .teamMembers(new HashSet<>(requestDTO.getTeamMembers()))
                .build();

        Team updatedTeam = teamRepository.save(team);
        return new TeamResponseDTO(
                updatedTeam.getId(),
                updatedTeam.getTeamName(),
                updatedTeam.getTeamMembers()
        );
    }

    @Override
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        teamRepository.delete(team);
    }
    @Override
    public Page<TeamResponseDTO> getAllTeamsPaginated(Pageable pageable) {
        Page<Team> teams = teamRepository.findAll(pageable);
        List<TeamResponseDTO> teamResponseDTOList = new ArrayList<>();

        for (Team team : teams) {
            TeamResponseDTO teamResponseDTO = new TeamResponseDTO(
                    team.getId(),
                    team.getTeamName(),
                    team.getTeamMembers()
            );
            teamResponseDTOList.add(teamResponseDTO);
        }

        return new PageImpl<>(teamResponseDTOList, pageable, teams.getTotalElements());
    }


    @Scheduled(cron = "0 11 13 * * ?")
    public void scheduleGenerateExcelAndSendEmail() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() == 13 && now.getMinute() == 11) {
            generateExcelAndSendEmail();
        }
    }
    @Override
    public Map<LocalDate, Set<Team>> generateMonthlyRoaster() {
        YearMonth yearMonth = YearMonth.now();
        Map<LocalDate, Set<Team>> roster = new LinkedHashMap<>();
        Random random = new Random();

        // Retrieve teams for the month
        List<Team> allTeams = new ArrayList<>(teamRepository.findAll());
        Collections.shuffle(allTeams);

        Map<Team, LocalDate> lastWorkDate = new HashMap<>();

        int maxTeamsPerDay = 2;

        for (int dayOfMonth = 1; dayOfMonth <= yearMonth.lengthOfMonth(); dayOfMonth++) {
            LocalDate date = yearMonth.atDay(dayOfMonth);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                continue;
            }

            Set<Team> teamsForDay = new HashSet<>();

            if (dayOfWeek == DayOfWeek.MONDAY) {
                teamsForDay.addAll(allTeams);
            } else if (dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.FRIDAY) {
                List<Team> availableTeams = new ArrayList<>(allTeams);

                int teamsToAdd = Math.min(maxTeamsPerDay, availableTeams.size());
                teamsForDay.addAll(availableTeams.subList(0, teamsToAdd));


                for (Team team : teamsForDay) {
                    lastWorkDate.put(team, date);
                }
            } else {

                Collections.shuffle(allTeams);

                for (Team team : allTeams) {
                    if (teamsForDay.size() >= 2) {
                        break;
                    }

                    LocalDate lastWorked = lastWorkDate.getOrDefault(team, LocalDate.MIN);
                    if (!lastWorked.plusDays(1).equals(date)) {
                        teamsForDay.add(team);
                        lastWorkDate.put(team, date);
                    }
                }

                if (teamsForDay.isEmpty()) {
                    // If no teams were selected, pick a random team
                    int randomIndex = random.nextInt(allTeams.size());
                    teamsForDay.add(allTeams.get(randomIndex));
                }
            }

            roster.put(date, teamsForDay);
        }
        return roster;
    }

    public void generateExcelAndSendEmail() {
        Map<LocalDate, Set<Team>> monthlyRoaster = generateMonthlyRoaster();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Monthly Roster");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Week Number");
        headerRow.createCell(1).setCellValue("Monday");
        headerRow.createCell(2).setCellValue("Tuesday");
        headerRow.createCell(3).setCellValue("Wednesday");
        headerRow.createCell(4).setCellValue("Thursday");
        headerRow.createCell(5).setCellValue("Friday");

        int rowNum = 1;
        int weekNumber = 1;

        for (Map.Entry<LocalDate, Set<Team>> entry : monthlyRoaster.entrySet()) {
            LocalDate date = entry.getKey();
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                continue;
            }

            if (dayOfWeek == DayOfWeek.MONDAY) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(weekNumber);

                int columnNum = 1;
                for (int i = 0; i < 5; i++) {
                    LocalDate currentDate = date.plusDays(i);
                    Set<Team> teamsForDay = monthlyRoaster.getOrDefault(currentDate, new HashSet<>());

                    StringBuilder teams = new StringBuilder();
                    teamsForDay.forEach(team -> teams.append(team.getTeamName()).append(", "));

                    Cell cell = dataRow.createCell(columnNum++);
                    cell.setCellValue(teams.toString());
                }

                weekNumber++;
            }
        }

        try {
            String excelFilePath = "Monthly_Roster.xlsx";
            FileOutputStream fileOut = new FileOutputStream(excelFilePath);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            // send email with Excel file
            // String attachmentFilePath = "path/to/your/file.xlsx";

            String emailHost = "smtp.gmail.com";
            String emailUsername = "toppytech@gmail.com";
            String emailPassword = "HFHFH";
            String recipientEmailAddress = "temitope321ayo@gmail.com";

            sendEmailWithAttachment(excelFilePath, emailHost, emailUsername, emailPassword, recipientEmailAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEmailWithAttachment(String attachmentPath, String host, String username, String password, String recipientEmail) {
        LocalDate localDate = LocalDate.now();
        Month currentMonth = localDate.getMonth();
        int currentYear = localDate.getYear();
        try {
            // Email properties
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", "587");

            // Get the Session object
            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Monthly Roaster");

            // Message body
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Please find attached" + " " + currentMonth + " " + currentYear + " "+ " roaster.");

            // Attachment
            BodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachmentPath);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName("MonthlyRoaster.xlsx");

            // Message content
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentBodyPart);
            message.setContent(multipart);

            // send email
            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }



    private boolean isTeamMemberExistsInOtherTeams (String teamMember){
            List<Team> teams = teamRepository.findAll();

            for (Team team : teams) {
                Set<String> teamMembers = team.getTeamMembers();
                if (teamMembers.contains(teamMember)) {
                    return true;
                }
            }
            return false;
        }

        private boolean areTeamMembersExistsInOtherTeams (Set < String > teamMembers) {
            List<Team> teams = teamRepository.findAll();

            for (Team team : teams) {
                Set<String> existingMembers = team.getTeamMembers();
                for (String member : teamMembers) {
                    if (existingMembers.contains(member)) {
                        return true;
                    }
                }
            }
            return false;
        }
        private boolean isTeamNameExists (String teamName){
            return teamRepository.existsByTeamName(teamName);
        }
}

//xkxpafoizgstwbaw







