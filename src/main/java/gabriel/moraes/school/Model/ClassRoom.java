package gabriel.moraes.school.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gabriel.moraes.school.Model.employee.Coordinator;
import gabriel.moraes.school.Model.employee.Instructor;
import gabriel.moraes.school.Model.employee.ScrumMaster;
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
public class ClassRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private ClassStatus status;

    @OneToOne
    @JoinColumn(name = "coordinator_id")
    private Coordinator coordinator;

    @OneToOne
    @JoinColumn(name = "scrum_master_id")
    private ScrumMaster scrumMaster;

    @OneToMany(mappedBy = "classRoom")
    private List<Instructor> instructors = new ArrayList<>();

    @OneToMany(mappedBy = "classRoom")
    private List<Student> students = new ArrayList<>();

    public ClassRoom(String name, Coordinator coordinator, ScrumMaster scrumMaster, List<Instructor> instructors, List<Student> students) {
        this.name = name;
        this.status = ClassStatus.WAITING;
        this.coordinator = coordinator;
        this.scrumMaster = scrumMaster;
        this.instructors = instructors;
        this.students = students;
    }
}