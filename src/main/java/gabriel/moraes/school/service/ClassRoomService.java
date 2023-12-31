package gabriel.moraes.school.service;

import gabriel.moraes.school.Model.*;
import gabriel.moraes.school.Model.DtoRequest.AddStudentsDtoRequest;
import gabriel.moraes.school.Model.DtoRequest.ClassRoomDtoRequest;
import gabriel.moraes.school.Model.DtoResponse.ClassRoomDtoResponse;
import gabriel.moraes.school.constants.ClassRoomConstants;
import gabriel.moraes.school.exception.*;
import gabriel.moraes.school.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClassRoomService {

    private static final int minStudent = ClassRoomConstants.MIN_STUDENTS;
    private static final int maxStudent = ClassRoomConstants.MAX_STUDENTS;
    private static final int maxInstructors = ClassRoomConstants.MAX_INSTRUCTORS;

    private final InstructorRepository instructorRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final StudentRepository studentRepository;
    private final ClassRoomRepository classRoomRepository;
    private final ScrumMasterRepository scrumMasterRepository;
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

    @Transactional(readOnly = true)
    public ClassRoomDtoResponse getClassById(Long id) {
        ClassRoom classRoom = findClassById(id);
        return mapper.map(classRoom, ClassRoomDtoResponse.class);
    }

    @Transactional
    public ClassRoomDtoResponse createClass(ClassRoomDtoRequest classDto) {

        List<Coordinator> coordinators = findCoordinatorById(classDto.getCoordinators());
        List<ScrumMaster> scrumMasters = findScrumMasterById(classDto.getScrumMasters());
        List<Instructor> instructors = findInstructorsByIds(classDto.getInstructors());

        ClassRoom classRoom = new ClassRoom(classDto.getName());

        classRoom.getCoordinators().addAll(coordinators);
        classRoom.getScrumMasters().addAll(scrumMasters);
        classRoom.getInstructors().addAll(instructors);

        ClassRoom savedClassRoom = classRoomRepository.save(classRoom);

        return mapper.map(savedClassRoom, ClassRoomDtoResponse.class);
    }

    @Transactional
    public ClassRoomDtoResponse addStudentsToClass(Long id, AddStudentsDtoRequest addStudentsDtoRequest) {

        ClassRoom classRoom = findClassById(id);
        List<Student> students = findStudentsByIds(addStudentsDtoRequest.getStudents());

        validateStudents(classRoom.getStudents());

        assignClassToStudents(students, classRoom);

        classRoom.getStudents().addAll(students);
        classRoomRepository.save(classRoom);

        return mapper.map(classRoom, ClassRoomDtoResponse.class);
    }

    private void assignClassToStudents(List<Student> students, ClassRoom classRoom) {
        if (classRoom.getStatus() != ClassStatus.WAITING) {
            throw new InvalidClassStatusException("It is only possible to add new students when the class room status is in WAITING");
        }

        for (Student student : students) {
            if (student.getClassRoom() != null) {
                throw new StudentAlreadyAssignedException("Student " + student.getFirstName() + "[ID: "+ student.getId()+"]"  + " is already assigned to a class.");
            }
            student.setClassRoom(classRoom);
        }
    }

    private ClassRoom findClassById(Long id) {
        return classRoomRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Class room not found with id: " + id));
    }

    private List<Coordinator> findCoordinatorById(List<Long> coordinatorIds) {
        List<Coordinator> coordinators = coordinatorRepository.findAllById(coordinatorIds);

        if (coordinatorIds.size() != coordinators.size()) {
            List<Long> notFoundIds = new ArrayList<>(coordinatorIds);
            notFoundIds.removeAll(coordinators.stream().map(Coordinator::getId).toList());
            throw new ObjectNotFoundException("Coordinators not found for IDs: " + notFoundIds);
        }
        return coordinators;
    }

    private List<ScrumMaster> findScrumMasterById(List<Long> scrumMasterIds) {
        List<ScrumMaster> scrumMasters = scrumMasterRepository.findAllById(scrumMasterIds);
        if (scrumMasterIds.size() != scrumMasters.size()) {
            List<Long> notFoundIds = new ArrayList<>(scrumMasterIds);
            notFoundIds.removeAll(scrumMasters.stream().map(ScrumMaster::getId).toList());
            throw new ObjectNotFoundException("Scrum Masters not found for IDs: " + notFoundIds);
        }
        return scrumMasters;
    }

    private List<Instructor> findInstructorsByIds(List<Long> instructorIds) {
        if (instructorIds.size() < maxInstructors) {
            throw new MinimumInstructorsException("Requires a minimum of 3 instructors");
        }
        List<Instructor> instructors = instructorRepository.findAllById(instructorIds);
        if (instructorIds.size() != instructors.size()) {
            List<Long> notFoundIds = new ArrayList<>(instructorIds);
            notFoundIds.removeAll(instructors.stream().map(Instructor::getId).toList());
            throw new ObjectNotFoundException("Instructors not found for IDs: " + notFoundIds);
        }
        return instructors;
    }

    private List<Student> findStudentsByIds(List<Long> studentIds) {
        List<Student> students = studentRepository.findAllById(studentIds);
        if (studentIds.size() != students.size()) {
            List<Long> notFoundIds = new ArrayList<>(studentIds);
            notFoundIds.removeAll(students.stream().map(Student::getId).toList());
            throw new ObjectNotFoundException("Students not found for IDs: " + notFoundIds);
        }
        return students;
    }

    private void validateStartStatus(ClassRoom classRoom) {
        int studentsCount = classRoom.getStudents().size();
        if (studentsCount < minStudent || studentsCount > maxStudent) {
            throw new InsufficientStudentsException("A minimum of 15 students is required to start a class.");
        }
        if (classRoom.getStatus() != ClassStatus.WAITING) {
            throw new InvalidClassStatusException("To start a class you need the status in WAITING");
        }

        classRoom.setStatus(ClassStatus.STARTED);
    }

    private void validateStudents(List<Student> students) {
        int studentsCount = students.size();
        if (studentsCount >= maxStudent) {
            throw new MaximumStudentsException("A class can have a maximum of 30 students");
        }
    }

    @Transactional
    public void finish(Long id) {
        ClassRoom classRoom = findClassById(id);

        if (classRoom.getStatus() == ClassStatus.STARTED) {
            classRoom.setStatus(ClassStatus.FINISHED);
        } else if (classRoom.getStatus() == ClassStatus.FINISHED) {
            throw new InvalidClassStatusException("Class room is already finished.");
        } else {
            throw new InvalidClassStatusException("Classroom needs to be in STARTED status to be finished.");
        }
    }

    @Transactional
    public void startClass(Long id) {
        ClassRoom classRoom = findClassById(id);
        validateStartStatus(classRoom);
    }
}