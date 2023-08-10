package gabriel.moraes.school.Controller;

import gabriel.moraes.school.Model.employee.DtoRequest.StudentDtoRequest;
import gabriel.moraes.school.Model.employee.DtoResponse.StudentDtoResponse;
import gabriel.moraes.school.Service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService){
        this.studentService = studentService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<StudentDtoResponse> getStudentById(@PathVariable Long id) {
        StudentDtoResponse dto = studentService.getStudentById(id);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<StudentDtoResponse>> getAllStudents() {
        List<StudentDtoResponse> dtos = studentService.getAllStudents();
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<StudentDtoResponse> save(@Valid @RequestBody StudentDtoRequest studentDtoRequest){
        StudentDtoResponse dto = studentService.save(studentDtoRequest);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
}