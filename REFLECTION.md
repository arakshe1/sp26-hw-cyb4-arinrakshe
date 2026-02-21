# Reflection (A4: RecipeService and Testing)

Complete all 6 questions below. Each question is worth 4 points (24 points total).

---

1. **Parsing Design:** How did you structure your parsing logic? Did you create separate classes
   (e.g., `IngredientParser`, `RecipeTextParser`) or keep it inline? What tradeoffs did you
   consider? If you were explaining your design choice to a skeptical teammate who preferred a
   different approach, what arguments would you use to advocate for your decision?

Answer: I structured the parsing logic into two dedicated helper classes: `IngredientParser` (for individual lines) and `RecipeTextParser` (for full text blocks). Inline parsing in the service would have made it unreadable and difficult to test. By separating them, I followed the Single Responsibility Principle. My argument to a skeptic would be: "Separating parsing allows us to refine regex logic and handle complex international formats independently of our persistence logic. It also honors the service layer's role as a coordinator rather than a low-level string manipulator."

---

2. **What Are Your Tests Actually Testing?** Look at your `RecipeServiceTest` suite. Are your tests
   primarily verifying _coordination_ (the service calls the right methods in the right order) or
   _computation_ (the service produces correct results)? Which type of bug would your tests catch?
   Which might they miss? Is that the right balance for a service layer?

Answer: My suite is a hybrid. For `scaleRecipe` and `importFromJson`, it tests *coordination* (verifying `save()` and `findById()` calls via ArgumentCaptors). For `generateShoppingList` and parsing, it tests *computation* (verifying quantities sum correctly and regex splits work). These tests catch logic errors (off-by-one, scaling math) but might miss environment-specific bugs (file permissions, database constraints). This is the correct balance for a service layer, as it ensures the "orchestrator" is calling its stage hands correctly.

---

3. **Implementing a Non-Ideal Interface:** The `RecipeService` facade bundles multiple
   responsibilities into single methods. How did you keep your _internal_ implementation clean
   despite this external constraint? What would you change about the interface if you could redesign
   it?

Answer: I used delegation. Instead of writing parsing logic inside the service methods, I created static `parse` methods in helper classes. This keeps the service methods' "cyclomatic complexity" low. If I could redesign it, I would split it into three interfaces: `RecipeParser` (returning POJOs), `RecipeRepository` (for storage), and `ShoppingListService` for aggregation. Forcing a recipe to be saved immediately upon parsing (`importFromText`) is a side-effect that might not always be desired.

---

4. **Mocks, Fakes, and Untestable Designs:** This assignment required mock-based testing for
   repositories, but `importFromJson` forced you to use real temp files because file I/O isn't
   behind a mockable interface. Compare these two testing approaches you used: (a) mocking
   `RecipeRepository` for methods like `scaleRecipe`, and (b) creating temp files for
   `importFromJson`. What bugs does each approach catch? What bugs might each miss? If you could
   redesign the `importFromJson` method signature to make it more testable, what would you change?
   What interface or abstraction would you introduce so that file reading could be mocked?

Answer: Mocking `RecipeRepository` catches coordination bugs (did we save the *new* recipe ID?) but misses serialization issues. Real temp files catch Jackson/JSON mismatch bugs and path handling errors but make tests slower and dependent on the disk. To improve testability, I would change `importFromJson` to accept an `InputStream` or `Reader`. Alternatively, I'd wrap file operations in a `FileIO` interface, allowing us to mock `fileIO.readAllBytes(path)` and return a hardcoded string.

---

5. **What the Struggle Taught You:** Describe a moment where you were stuck on this assignment. What
   was confusing? How did you get unstuck (office hours, debugging, stepping away, etc.)? What did
   this experience reveal about how you work best — do you prefer to push through, step away, seek
   help early, or something else? How might you approach a similar situation differently next time?

Answer: I got stuck on a persistent `Type T not present` Gradle evaluation failure. It was confusing because it appeared to be a framework bug rather than a code error. I got unstuck by systematically simplifying the `build.gradle` and converting older DSL blocks to newer "lazy" `tasks.named().configure` patterns. This confirmed that I prefer to "push through" and understand the root cause of infrastructure failures rather than just working around them. Next time, I'll trust my intuition earlier when a build system error feels like an environment mismatch.

---

6. **AI Collaboration:** Which tasks benefited most from AI assistance (e.g., boilerplate, parsing
   logic, test generation, debugging)? Where did you need to think independently? Did the AI teach
   you anything new — a technique, pattern, or concept you hadn't seen before? What's one thing you
   learned about working effectively with AI on this assignment?

Answer: AI was invaluable for writing the complex Regex for `IngredientParser` and generating the initial 40+ test assertions in the suite. I had to think independently when debugging the Gradle/JDK version incompatibility and ensuring NullAway compliance. I learned the power of `LinkedHashMap` for maintaining encounter order during deduplication—a pattern I hadn't used extensively before. The key to AI collaboration here was modularity; the AI was far more effective when parsing was isolated from the service.
