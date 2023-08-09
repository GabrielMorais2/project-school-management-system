package gabriel.moraes.school.Service;

import gabriel.moraes.school.Model.ClassRoom;
import gabriel.moraes.school.Model.ClassStatus;
import gabriel.moraes.school.Model.Student;
import gabriel.moraes.school.Model.employee.Coordinator;
import gabriel.moraes.school.Model.employee.DtoRequest.AddStudentsDtoRequest;
import gabriel.moraes.school.Model.employee.DtoRequest.ClassRoomDtoRequest;
import gabriel.moraes.school.Model.employee.DtoResponse.ClassRoomDtoResponse;
import gabriel.moraes.school.Model.employee.Instructor;
import gabriel.moraes.school.Model.employee.ScrumMaster;
import gabriel.moraes.school.exception.InvalidClassStatusException;
import gabriel.moraes.school.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassRoomService {

    private final InstructorRepository instructorRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final StudentRepository studentRepository;
    private final ScrumMasterRepository scrumMasterRepository;
    private final ClassRoomRepository classRoomRepository;
    private final ModelMapper mapper;

    public ClassRoomService(InstructorRepository instructorRepository, ModelMapper mapper,
                            ScrumMasterRepository scrumMasterRepository, StudentRepository studentRepository,
                            CoordinatorRepository coordinatorRepository, ClassRoomRepository classRoomRepository) {
        this.instructorRepository = instructorRepository;
        this.scrumMasterRepository = scrumMasterRepository;
        this.studentRepository = studentRepository;
        this.coordinatorRepository = coordinatorRepository;
        this.classRoomRepository = classRoomRepository;
        this.mapper = mapper;
    }

    // Método para obter os detalhes de uma turma por ID
    public ClassRoomDtoResponse getClassById(Long id) {
        ClassRoom classRoom = findClassById(id);
        return mapper.map(classRoom, ClassRoomDtoResponse.class);
    }

    // Método para criar uma turma
    public ClassRoomDtoResponse createClass(ClassRoomDtoRequest classDto) {
        Coordinator coordinator = findCoordinatorById(classDto.getCoordinator());
        ScrumMaster scrumMaster = findScrumMasterById(classDto.getScrumMaster());
        List<Instructor> instructors = findInstructorsByIds(classDto.getInstructors());

        validateInstructors(instructors);

        ClassRoom classRoom = new ClassRoom(classDto.getName(), coordinator, scrumMaster, instructors);

        assignClassToInstructors(instructors, classRoom);

        ClassRoom savedClassRoom = classRoomRepository.save(classRoom);

        return mapper.map(savedClassRoom, ClassRoomDtoResponse.class);
    }

    // Método para iniciar uma turma
    public void startClass(Long id) {
        ClassRoom classRoom = findClassById(id);
        validateStatus(classRoom);
    }

    // Método para adicionar alunos a uma turma
    public ClassRoomDtoResponse addStudentsToClass(Long id, AddStudentsDtoRequest addStudentsDtoRequest) {
        ClassRoom classRoom = findClassById(id);
        List<Student> students = findStudentsByIds(addStudentsDtoRequest.getStudents());

        validateStudents(students);

        assignClassToStudents(students, classRoom);

        classRoom.getStudents().addAll(students);
        classRoomRepository.save(classRoom);

        return mapper.map(classRoom, ClassRoomDtoResponse.class);
    }

    // Métodos auxiliares para encontrar entidades por ID
    private ClassRoom findClassById(Long id) {
        return classRoomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ClassRoom not found with id: " + id));
    }

    private Coordinator findCoordinatorById(Long coordinatorId) {
        return coordinatorRepository.findById(coordinatorId)
                .orElseThrow(() -> new EntityNotFoundException("Coordinator not found"));
    }

    private ScrumMaster findScrumMasterById(Long scrumMasterId) {
        return scrumMasterRepository.findById(scrumMasterId)
                .orElseThrow(() -> new EntityNotFoundException("Scrum Master not found"));
    }

    private List<Instructor> findInstructorsByIds(List<Long> instructorIds) {
        return instructorRepository.findAllById(instructorIds);
    }

    private List<Student> findStudentsByIds(List<Long> studentIds) {
        return studentRepository.findAllById(studentIds);
    }

    // Métodos auxiliares para validações
    private void validateStatus(ClassRoom classRoom) {
        int studentsCount = classRoom.getStudents().size();
        if (studentsCount < 15 || studentsCount > 30) {
            throw new IllegalArgumentException("A minimum of 15 students is required to start a class.");
        }

        if (classRoom.getStatus() == ClassStatus.WAITING) {
            classRoom.setStatus(ClassStatus.STARTED);
            classRoomRepository.save(classRoom);
        } else {
            throw new InvalidClassStatusException("ClassRoom is not in waiting status");
        }
    }

    private void validateInstructors(List<Instructor> instructors) {
        if (instructors.size() < 3) {
            throw new IllegalArgumentException("Requires a minimum of 3 instructors");
        }
    }

    private void validateStudents(List<Student> students) {
        int studentsCount = students.size();
        if (studentsCount > 30) {
            throw new IllegalArgumentException("Requires a minimum of 15 students and a maximum of 30");
        }
    }

    // Métodos auxiliares para atribuir turma a alunos e instrutores
    private void assignClassToStudents(List<Student> students, ClassRoom classRoom) {

        if(classRoom.getStatus() == ClassStatus.WAITING){
            students.forEach(student -> student.setClassRoom(classRoom));
        } else {
            throw new InvalidClassStatusException("ClassRoom is not in waiting status");
        }

        for (Student student : students) {
            if (student.getClassRoom() != null) {
                throw new IllegalArgumentException("Student " + student.getName() + " is already assigned to a class.");
            }
        }

    }

    private void assignClassToInstructors(List<Instructor> instructors, ClassRoom classRoom) {
        for (Instructor instructor : instructors) {
            if (instructor.getClassRoom() != null) {
                throw new IllegalArgumentException("Instructor " + instructor.getName() + " is already assigned to a class.");
            }
        }

        instructors.forEach(instructor -> instructor.setClassRoom(classRoom));
    }

}