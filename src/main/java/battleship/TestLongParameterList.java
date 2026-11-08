package battleship;

public class TestLongParameterList {
    public void registerNewUser(
            String firstName,
            String lastName,
            String emailAddress,
            String password,
            String phoneNumber,
            String homeAddress,
            String dateOfBirth
    ) {
        System.out.println("A registar utilizador: " + firstName + " " + lastName);
        System.out.println("Email: " + emailAddress);
        System.out.println("Telefone: " + phoneNumber);
        System.out.println("Morada: " + homeAddress);
        System.out.println("Data de Nascimento: " + dateOfBirth);

        // Simulação de lógica de negócio
        saveToDatabase(firstName, lastName, emailAddress, password);
    }

    private void saveToDatabase(String firstName, String lastName, String email, String password) {
        // Lógica fictícia de persistência
    }
}
