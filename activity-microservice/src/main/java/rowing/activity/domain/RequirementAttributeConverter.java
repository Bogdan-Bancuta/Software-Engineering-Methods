package rowing.activity.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA Converter for the NetID value object.
 */
@Converter
public class RequirementAttributeConverter implements AttributeConverter<Requirement, String> {

    @Override
    public String convertToDatabaseColumn(Requirement attribute) {
        return attribute.toString();
    }

    @Override
    public Requirement convertToEntityAttribute(String dbData) {
        //To do
        return new Requirement();
    }

}
