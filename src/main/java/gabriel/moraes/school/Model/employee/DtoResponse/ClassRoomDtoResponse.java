package gabriel.moraes.school.Model.employee.DtoResponse;

import gabriel.moraes.school.Model.ClassStatus;
import gabriel.moraes.school.Model.Student;
import gabriel.moraes.school.Model.employee.Coordinator;
import gabriel.moraes.school.Model.employee.Instructor;
import gabriel.moraes.school.Model.employee.ScrumMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassRoomDtoResponse {

    private Long id;
    private String name;
    private ClassStatus status;
    private Coordinator coordinator;
    private ScrumMaster scrumMaster;
    private List<Instructor> instructors = new ArrayList<>();
    private List<Student> students = new ArrayList<>();

}