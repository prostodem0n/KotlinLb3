import kotlinx.coroutines.*

// Student class
class Student private constructor(
    private var _name: String,
    private var _age: Int,
    private var _grades: MutableList<Int>
) {
    init {
        println("Student created: ${_name}, age: ${_age}")
    }

    // Secondary constructor: only name
    constructor(name: String) : this(name, 18, mutableListOf())

    // Property name: trims whitespace and capitalizes first letter
    var name: String
        get() = _name
        set(value) {
            _name = value.trim().replaceFirstChar { it.uppercase() }
        }

    // Property age: only set if ≥ 0
    var age: Int
        get() = _age
        set(value) {
            if (value >= 0) {
                _age = value
            } else {
                println("Error: age cannot be negative")
            }
        }

    // Property grades
    var grades: List<Int>
        get() = _grades.toList() // Return a copy of the list for encapsulation
        private set(value) {
            _grades = value.toMutableList()
        }

    // isAdult: Boolean — property with getter
    val isAdult: Boolean
        get() = _age >= 18

    // status: String — property with by lazy
    val status: String by lazy {
        if (isAdult) "Adult" else "Minor"
    }

    // Function getAverage(): returns average grade
    fun getAverage(): Double {
        if (_grades.isEmpty()) return 0.0
        return _grades.average()
    }

    // Function processGrades(operation: (Int) -> Int): modifies all grades according to the passed function
    fun processGrades(operation: (Int) -> Int) {
        _grades = _grades.map { operation(it) }.toMutableList()
    }

    // Function updateGrades(grades: List<Int>): updates grades
    fun updateGrades(grades: List<Int>) {
        _grades.clear()
        _grades.addAll(grades)
        println("Grades updated for student $name: ${_grades.joinToString()}")
    }

    // Operator overloading
    // «+» — combines grades of two students
    operator fun plus(other: Student): Student {
        val combinedGrades = this._grades + other._grades
        return Student(this._name, this._age, combinedGrades.toMutableList())
    }

    // «*» — multiplies all grades by a number
    operator fun times(multiplier: Int): Student {
        val newGrades = _grades.map { it * multiplier }.toMutableList()
        return Student(this._name, this._age, newGrades)
    }

    // «==» — compares students by name and average grade
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Student) return false
        return this._name == other._name && this.getAverage() == other.getAverage()
    }

    override fun hashCode(): Int {
        var result = _name.hashCode()
        result = 31 * result + getAverage().hashCode()
        return result
    }

    override fun toString(): String {
        return "Student(name='$_name', age=$_age, grades=${_grades.joinToString()}, average=${getAverage()}, isAdult=$isAdult)"
    }
}

// Group class
class Group(vararg val students: Student) {
    // operator fun get(index: Int): allows access to students by index
    operator fun get(index: Int): Student {
        return students[index]
    }

    // Function getTopStudent(): returns student with highest average grade
    fun getTopStudent(): Student? {
        if (students.isEmpty()) return null
        return students.maxByOrNull { it.getAverage() }
    }

    override fun toString(): String {
        return "Group(students=${students.joinToString("\n")})"
    }
}

// Asynchronous logic
suspend fun fetchGradesFromServer(): List<Int> {
    println("Fetching grades from server...")
    delay(2000) // simulating server request
    val grades = listOf(85, 90, 78, 92, 88)
    println("Grades received from server: ${grades.joinToString()}")
    return grades
}

fun main() = runBlocking {
    println("===== Student Grading System =====")

    // Creating students
    val student1 = Student("ivan petrenko")
    student1.age = 20  // Changing age after creation
    student1.updateGrades(listOf(75, 82, 90, 68))

    println("\n--- Student Information ---")
    println(student1)

    // Using lazy property
    println("Student status: ${student1.status}")
    println()

    // Secondary constructor + named arguments
    val student2 = Student("maria").apply {
        age = 17
        updateGrades(listOf(95, 88, 92, 90))
    }

    println("\n--- Second Student Information ---")
    println(student2)
    println("Student status: ${student2.status}")

    // Using overloaded operators
    println("\n--- Operators Demonstration ---")
    val combinedStudent = student1 + student2
    println("Combined student (student1 + student2): $combinedStudent")
    println()

    val multipliedStudent = student1 * 2
    println("Student with doubled grades (student1 * 2): $multipliedStudent")
    println()

    println("student1 == student2: ${student1 == student2}")

    // Using processGrades with lambda function
    println("\n--- Grades Processing ---")
    val originalGrades = student1.grades
    println("Original grades: ${originalGrades.joinToString()}")
    println()

    student1.processGrades { grade -> grade + 5 }
    println("Grades after adding 5 points: ${student1.grades.joinToString()}")
    println()

    // Creating a group
    val student3 = Student("alexander").apply {
        age = 20
        updateGrades(listOf(78, 85, 92, 88))
    }

    println("\n--- Student Group ---")
    val group = Group(student1, student2, student3)
    println("Student with index 1: ${group[1]}")
    println("Student with highest average grade: ${group.getTopStudent()}")

    // Asynchronous grade update
    println("\n--- Asynchronous Grade Update ---")

    val fetchedGrades = async { fetchGradesFromServer() }
    println("Waiting for grades...")

    val newGrades = fetchedGrades.await()
    println("Grades received, updating data...")

    student3.updateGrades(newGrades)
    println("Updated student information: $student3")

    println("\n===== Program Completed =====")
}