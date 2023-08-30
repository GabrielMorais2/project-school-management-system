package gabriel.moraes.school.domain.scrummaster;

import com.fasterxml.jackson.annotation.JsonBackReference;
import gabriel.moraes.school.domain.classroom.ClassRoom;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScrumMaster{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @ManyToMany(mappedBy = "scrumMasters")
    @JsonBackReference(value = "classRoom-scrum-masters")
    private List<ClassRoom> classRooms = new ArrayList<>();

    public ScrumMaster(Long id, String firstName, String lastName, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }
}