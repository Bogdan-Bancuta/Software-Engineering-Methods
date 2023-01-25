package rowing.activity.domain;

import rowing.activity.domain.entities.Competition;
import rowing.commons.Gender;
import rowing.commons.Position;
import rowing.commons.entities.ActivityDTO;
import rowing.commons.entities.CompetitionDTO;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Director {

    /**
     * Constructor for the competition out of raw attributes.
     *
     * @param builder that will build
     * @param id of the activity
     * @param name of the activity
     * @param owner of the activity
     * @param type training in our case
     * @param start time the competition starts
     * @param positions list of positions to be filled
     * @param applicants list of applicants names
     */
    public void constructTraining(TrainingBuilder builder, UUID id, String name, String owner, String type,
                                  Date start, String location,
                                  List<Position> positions, List<String> applicants, String boatType) {
        builder.setId(id);
        builder.setName(name);
        builder.setOwner(owner);
        builder.setType(type);
        builder.setStart(start);
        builder.setLocation(location);
        builder.setPositions(positions);
        builder.setApplicants(applicants);
        builder.setBoatType(boatType);
    }

    /**
     * Function that will set training activity settings in order to build from a DTO.
     *
     * @param builder that will build the activity
     * @param dto the dto that contains the information regarding activity parameters
     */
    public void constructTrainingDTO(TrainingBuilder builder, ActivityDTO dto) {
        builder.setId(dto.getId());
        builder.setName(dto.getName());
        builder.setOwner(dto.getOwner());
        builder.setType("Training");
        builder.setStart(dto.getStart());
        builder.setLocation(dto.getLocation());
        builder.setPositions(dto.getPositions());
        builder.setApplicants(dto.getApplicants());
        builder.setBoatType(dto.getBoatType());
    }

    /**
     * Constructor for the competition out of raw attributes.
     *
     * @param builder that will build
     * @param id of the activity
     * @param name of the activity
     * @param owner of the activity
     * @param type competition in our case
     * @param start time the competition starts
     * @param gender non-null if it's gender locked
     * @param organisation non-null if it's organisation restrictive
     * @param positions list of positions to be filled
     * @param applicants list of applicants names
     */
    public void constructCompetition(CompetitionBuilder builder, UUID id, String name, String owner, String type,
                                     Date start, String location, Gender gender, String organisation,
                                     List<Position> positions, List<String> applicants, String boatType) {
        builder.setId(id);
        builder.setName(name);
        builder.setOwner(owner);
        //builder.setType("Competition");
        builder.setStart(start);
        builder.setLocation(location);
        builder.setGender(gender);
        builder.setOrganisation(organisation);
        builder.setPositions(positions);
        builder.setApplicants(applicants);
        builder.setBoatType(boatType);
    }

    /**
     * Function that will set competition activity settings in order to build from a DTO.
     *
     * @param builder that will build the activity
     * @param dto the dto that contains the information regarding activity parameters
     */
    public void constructCompetitionDTO(CompetitionBuilder builder, CompetitionDTO dto) {
        builder.setId(dto.getId());
        builder.setName(dto.getName());
        builder.setOwner(dto.getOwner());
        //builder.setType("Competition");
        builder.setStart(dto.getStart());
        builder.setLocation(dto.getLocation());
        builder.setPositions(dto.getPositions());
        builder.setGender(dto.getGender());
        builder.setOrganisation(dto.getOrganisation());
        builder.setApplicants(dto.getApplicants());
        builder.setBoatType(dto.getBoatType());
    }
}
