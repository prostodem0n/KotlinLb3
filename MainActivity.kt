import kotlinx.coroutines.*

class Student private constructor(
    private var _name: String,
    private var _age: Int,
    private var _grades: MutableList<Int>
) {
    init {
        println("Створено студента: ${_name}, вік: ${_age}")
    }

    // Вторинний конструктор: тільки ім'я
    constructor(name: String) : this(name, 18, mutableListOf())

    // Властивість ім'я: обрізає пробіли та робить першу літеру великою
    var name: String
        get() = _name
        set(value) {
            _name = value.trim().replaceFirstChar { it.uppercase() }
        }

    // Властивість вік: встановлюється лише якщо ≥ 0
    var age: Int
        get() = _age
        set(value) {
            if (value >= 0) {
                _age = value
            } else {
                println("Помилка: вік не може бути від'ємним")
            }
        }

    // Властивість оцінки
    var grades: List<Int>
        get() = _grades.toList() // Повертає копію списку для інкапсуляції
        private set(value) {
            _grades = value.toMutableList()
        }

    // isAdult: Boolean — властивість з геттером
    val isAdult: Boolean
        get() = _age >= 18

    // status: String — властивість з by lazy
    val status: String by lazy {
        if (isAdult) "Дорослий" else "Неповнолітній"
    }

    // Функція getAverage(): повертає середній бал
    fun getAverage(): Double {
        if (_grades.isEmpty()) return 0.0
        return _grades.average()
    }

    // Функція processGrades(operation: (Int) -> Int): змінює всі оцінки згідно з переданою функцією
    fun processGrades(operation: (Int) -> Int) {
        _grades = _grades.map { operation(it) }.toMutableList()
    }

    // Функція updateGrades(grades: List<Int>): оновлює оцінки
    fun updateGrades(grades: List<Int>) {
        _grades.clear()
        _grades.addAll(grades)
        println("Оцінки оновлено для студента $name: ${_grades.joinToString()}")
    }

    // Перевантаження операторів
    // «+» — об'єднує оцінки двох студентів
    operator fun plus(other: Student): Student {
        val combinedGrades = this._grades + other._grades
        return Student(this._name, this._age, combinedGrades.toMutableList())
    }

    // «*» — множить всі оцінки на число
    operator fun times(multiplier: Int): Student {
        val newGrades = _grades.map { it * multiplier }.toMutableList()
        return Student(this._name, this._age, newGrades)
    }

    // «==» — порівнює студентів за ім'ям та середнім балом
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
        return "Student(ім'я='$_name', вік=$_age, оцінки=${_grades.joinToString()}, середній бал=${getAverage()}, повнолітній=$isAdult)"
    }
}

// Клас Group
class Group(vararg val students: Student) {
    // operator fun get(index: Int): дозволяє звертатись до студентів за індексом
    operator fun get(index: Int): Student {
        return students[index]
    }

    // Функція getTopStudent(): повертає студента з найвищим середнім балом
    fun getTopStudent(): Student? {
        if (students.isEmpty()) return null
        return students.maxByOrNull { it.getAverage() }
    }

    override fun toString(): String {
        return "Group(студенти=${students.joinToString("\n")})"
    }
}

// Асинхронна логіка
suspend fun fetchGradesFromServer(): List<Int> {
    println("Отримання оцінок з сервера...")
    delay(2000) // імітація запиту до сервера
    val grades = listOf(85, 90, 78, 92, 88)
    println("Отримано оцінки з сервера: ${grades.joinToString()}")
    return grades
}

fun main() = runBlocking {
    println("===== Система оцінювання студентів =====")

    // Створення студентів
    val student1 = Student("Іван Петренко")
    student1.age = 20  // Зміна віку після створення
    student1.updateGrades(listOf(75, 82, 90, 68))

    println("\n--- Інформація про студента ---")
    println(student1)

    // Використання lazy властивості
    println("Статус студента: ${student1.status}")
    println()

    // Вторинний конструктор + іменовані аргументи
    val student2 = Student(name = "Марія").apply {
        age = 17
        updateGrades(listOf(95, 88, 92, 90))
    }

    println("\n--- Інформація про другого студента ---")
    println(student2)
    println("Статус студента: ${student2.status}")

    // Використання перевантажених операторів
    println("\n--- Демонстрація операторів ---")
    val combinedStudent = student1 + student2
    println("Об'єднаний студент (student1 + student2): $combinedStudent")
    println()

    val multipliedStudent = student1 * 2
    println("Студент з подвоєними оцінками (student1 * 2): $multipliedStudent")
    println()

    println("student1 == student2: ${student1 == student2}")

    // Використання processGrades з лямбда-функцією
    println("\n--- Обробка оцінок ---")
    val originalGrades = student1.grades
    println("Початкові оцінки: ${originalGrades.joinToString()}")
    println()

    student1.processGrades { grade -> grade + 5 }
    println("Оцінки після додавання 5 балів: ${student1.grades.joinToString()}")
    println()

    // Створення групи
    val student3 = Student("олександр").apply {
        age = 20
        updateGrades(listOf(78, 85, 92, 88))
    }

    println("\n--- Група студентів ---")
    val group = Group(student1, student2, student3)
    println("Студент з індексом 1: ${group[1]}")
    println("Студент з найвищим середнім балом: ${group.getTopStudent()}")

    // Асинхронне оновлення оцінок
    println("\n--- Асинхронне оновлення оцінок ---")

    val fetchedGrades = async { fetchGradesFromServer() }
    println("Очікування оцінок...")

    val newGrades = fetchedGrades.await()
    println("Отримано оцінки, оновлення даних...")

    student3.updateGrades(newGrades)
    println("Оновлена інформація про студента: $student3")

    println("\n===== Програма завершена =====")
}