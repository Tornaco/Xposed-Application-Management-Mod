package github.tornaco.repo;

public class RepoGenerator {

    public static void main(String[] args) {

    }

    class VerifierRepoGenerator {

        static final String BASE_URL = "https://github.com/Tornaco/XAppGuard";

        void generate() {
            VerifierApp verifierApp = new VerifierApp();
            verifierApp.setName("SMALLED VERIFIER");
            verifierApp.setIconUrl(BASE_URL + "smaller/icon.png");
            verifierApp.setDescription("DESC");
            verifierApp.setUrl(BASE_URL);
        }
    }
}
