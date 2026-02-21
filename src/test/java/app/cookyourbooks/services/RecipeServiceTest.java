package app.cookyourbooks.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.cookyourbooks.conversion.ConversionRegistry;
import app.cookyourbooks.exception.UnsupportedConversionException;
import app.cookyourbooks.model.ExactQuantity;
import app.cookyourbooks.model.Ingredient;
import app.cookyourbooks.model.MeasuredIngredient;
import app.cookyourbooks.model.PersonalCollectionImpl;
import app.cookyourbooks.model.Recipe;
import app.cookyourbooks.model.RecipeCollection;
import app.cookyourbooks.model.Servings;
import app.cookyourbooks.model.ShoppingItem;
import app.cookyourbooks.model.ShoppingList;
import app.cookyourbooks.model.Unit;
import app.cookyourbooks.model.VagueIngredient;
import app.cookyourbooks.repository.RecipeCollectionRepository;
import app.cookyourbooks.repository.RecipeRepository;

/** Unit tests for {@link RecipeService} using Mockito to mock repository dependencies. */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway.Init") // Mockito initializes @Mock fields
class RecipeServiceTest {

  @Mock private RecipeRepository recipeRepository;
  @Mock private RecipeCollectionRepository collectionRepository;
  @Mock private ConversionRegistry conversionRegistry;

  private RecipeService service;

  @BeforeEach
  void setUp() {
    service = new RecipeServiceImpl(recipeRepository, collectionRepository, conversionRegistry);
  }

  // ==================== Helper Methods ====================

  /** Creates a personal recipe collection with the given id and title. */
  RecipeCollection createCollection(String id, String title) {
    return PersonalCollectionImpl.builder().id(id).title(title).build();
  }

  /** Creates a recipe with servings and ingredients. */
  Recipe createRecipe(String id, String title, int servings, List<Ingredient> ingredients) {
    return new Recipe(id, title, new Servings(servings), ingredients, List.of(), List.of());
  }

  /** Creates a recipe with servings but no ingredients. */
  Recipe createRecipe(String id, String title, int servings) {
    return new Recipe(id, title, new Servings(servings), List.of(), List.of(), List.of());
  }

  /** Creates a recipe with no servings. */
  Recipe createRecipeNoServings(String id, String title) {
    return new Recipe(id, title, null, List.of(), List.of(), List.of());
  }

  /** Creates a measured ingredient with an exact quantity. */
  MeasuredIngredient measured(String name, double amount, Unit unit) {
    return new MeasuredIngredient(name, new ExactQuantity(amount, unit), null, null);
  }

  /** Creates a vague ingredient. */
  VagueIngredient vague(String name) {
    return new VagueIngredient(name, null, null, null);
  }

  /** Stubs recipeRepository.findById for each given recipe. */
  void givenRecipesExist(Recipe... recipes) {
    for (Recipe r : recipes) {
      when(recipeRepository.findById(r.getId())).thenReturn(Optional.of(r));
    }
  }

  /** Stubs collectionRepository.findById for the given collection. */
  void givenCollectionExists(RecipeCollection collection) {
    when(collectionRepository.findById(collection.getId())).thenReturn(Optional.of(collection));
  }

  // ==================== importFromJson ====================

  @Nested
  @DisplayName("importFromJson")
  class ImportFromJson {

    @TempDir Path tempDir;

    @Test
    @DisplayName("throws CollectionNotFoundException when collection does not exist")
    void throwsWhenCollectionNotFound() {
      when(collectionRepository.findById("no-such-col")).thenReturn(Optional.empty());
      Path fakeFile = tempDir.resolve("recipe.json");

      assertThatThrownBy(() -> service.importFromJson(fakeFile, "no-such-col"))
          .isInstanceOf(CollectionNotFoundException.class);
    }

    @Test
    @DisplayName("checks collection existence BEFORE reading the file")
    void checksCollectionBeforeFile() {
      // Collection doesn't exist, file also doesn't exist — should get CollectionNotFoundException
      // not ImportException or any I/O error
      when(collectionRepository.findById("bad-col")).thenReturn(Optional.empty());
      Path nonExistent = tempDir.resolve("nonexistent.json");

      assertThatThrownBy(() -> service.importFromJson(nonExistent, "bad-col"))
          .isInstanceOf(CollectionNotFoundException.class);
    }

    @Test
    @DisplayName("throws ImportException when file does not exist")
    void throwsImportExceptionForMissingFile() {
      RecipeCollection col = createCollection("col-1", "My Collection");
      givenCollectionExists(col);
      Path missing = tempDir.resolve("does-not-exist.json");

      assertThatThrownBy(() -> service.importFromJson(missing, col.getId()))
          .isInstanceOf(ImportException.class);
    }

    @Test
    @DisplayName("throws ImportException when file contains invalid JSON")
    void throwsImportExceptionForInvalidJson() throws IOException {
      RecipeCollection col = createCollection("col-1", "My Collection");
      givenCollectionExists(col);
      Path badJson = tempDir.resolve("bad.json");
      Files.writeString(badJson, "not valid json {{{{");

      assertThatThrownBy(() -> service.importFromJson(badJson, col.getId()))
          .isInstanceOf(ImportException.class);
    }

    @Test
    @DisplayName("saves recipe to repository when import succeeds")
    void savesRecipeToRepository() throws IOException {
      RecipeCollection col = createCollection("col-1", "My Collection");
      givenCollectionExists(col);

      // Write a minimal valid recipe JSON
      Path recipeJson = tempDir.resolve("recipe.json");
      String json =
          """
          {
            "id": "r-001",
            "title": "Test Recipe",
            "servings": {"amount": 2},
            "ingredients": [],
            "instructions": [],
            "conversionRules": []
          }
          """;
      Files.writeString(recipeJson, json);

      Recipe result = service.importFromJson(recipeJson, col.getId());

      assertThat(result.getTitle()).isEqualTo("Test Recipe");
      assertThat(result.getId()).isEqualTo("r-001");

      ArgumentCaptor<Recipe> captor = forClass(Recipe.class);
      verify(recipeRepository).save(captor.capture());
      assertThat(captor.getValue().getId()).isEqualTo("r-001");
    }

    @Test
    @DisplayName("updates collection in repository after import")
    void updatesCollection() throws IOException {
      RecipeCollection col = createCollection("col-1", "My Collection");
      givenCollectionExists(col);

      Path recipeJson = tempDir.resolve("recipe.json");
      String json =
          """
          {
            "id": "r-001",
            "title": "Test Recipe",
            "servings": {"amount": 2},
            "ingredients": [],
            "instructions": [],
            "conversionRules": []
          }
          """;
      Files.writeString(recipeJson, json);

      service.importFromJson(recipeJson, col.getId());

      ArgumentCaptor<RecipeCollection> captor = forClass(RecipeCollection.class);
      verify(collectionRepository).save(captor.capture());
      assertThat(captor.getValue().getRecipes()).hasSize(1);
      assertThat(captor.getValue().getRecipes().get(0).getId()).isEqualTo("r-001");
    }
  }

  // ==================== importFromText ====================

  @Nested
  @DisplayName("importFromText")
  class ImportFromText {

    @Test
    @DisplayName("throws CollectionNotFoundException when collection does not exist")
    void throwsWhenCollectionNotFound() {
      when(collectionRepository.findById("bad")).thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  service.importFromText(
                      "Pancakes\nServings: 4\nIngredients:\n2 cups flour\n", "bad"))
          .isInstanceOf(CollectionNotFoundException.class);
    }

    @Test
    @DisplayName("checks collection BEFORE attempting to parse")
    void checksCollectionBeforeParsing() {
      // Blank text would cause ParseException, but collection check comes first
      when(collectionRepository.findById("bad")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.importFromText("   ", "bad"))
          .isInstanceOf(CollectionNotFoundException.class);
    }

    @Test
    @DisplayName("throws ParseException for blank text")
    void throwsParseExceptionForBlankText() {
      RecipeCollection col = createCollection("col-1", "My Col");
      givenCollectionExists(col);

      assertThatThrownBy(() -> service.importFromText("   \n   ", col.getId()))
          .isInstanceOf(ParseException.class);
    }

    @Test
    @DisplayName("parses title from first non-blank line")
    void parsesTitleFromFirstLine() throws ParseException {
      RecipeCollection col = createCollection("col-1", "My Col");
      givenCollectionExists(col);

      Recipe result = service.importFromText("Chocolate Chip Cookies\n", col.getId());

      assertThat(result.getTitle()).isEqualTo("Chocolate Chip Cookies");
    }

    @Test
    @DisplayName("parsed recipe is saved to recipeRepository")
    void savedToRecipeRepository() throws ParseException {
      RecipeCollection col = createCollection("col-1", "My Col");
      givenCollectionExists(col);

      service.importFromText("Banana Bread\n", col.getId());

      verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("collection is updated with the imported recipe")
    void collectionUpdatedWithRecipe() throws ParseException {
      RecipeCollection col = createCollection("col-1", "My Col");
      givenCollectionExists(col);

      service.importFromText("Banana Bread\n", col.getId());

      ArgumentCaptor<RecipeCollection> captor = forClass(RecipeCollection.class);
      verify(collectionRepository).save(captor.capture());
      assertThat(captor.getValue().getRecipes()).hasSize(1);
    }

    @Test
    @DisplayName("parses measured ingredients from ingredients section")
    void parsesMeasuredIngredients() throws ParseException {
      RecipeCollection col = createCollection("col-1", "My Col");
      givenCollectionExists(col);
      String text =
          """
          Simple Bread
          Ingredients:
          2 cups flour
          1 tsp salt
          """;

      Recipe result = service.importFromText(text, col.getId());

      assertThat(result.getIngredients()).hasSize(2);
      assertThat(result.getIngredients().get(0)).isInstanceOf(MeasuredIngredient.class);
      assertThat(result.getIngredients().get(0).getName()).isEqualTo("flour");
    }

    @Test
    @DisplayName("parses servings with 'Serves N' line")
    @SuppressWarnings("NullAway")
    void parsesServings() throws ParseException {
      RecipeCollection col = createCollection("col-1", "My Col");
      givenCollectionExists(col);
      String text =
          """
          Pancakes
          Serves 4
          Ingredients:
          """;

      Recipe result = service.importFromText(text, col.getId());

      assertThat(result.getServings()).isNotNull();
      assertThat(result.getServings().getAmount()).isEqualTo(4);
    }

    @Test
    @DisplayName("parses instructions from instructions section")
    void parsesInstructions() throws ParseException {
      RecipeCollection col = createCollection("col-1", "My Col");
      givenCollectionExists(col);
      String text =
          """
          Quick Rice
          Instructions:
          1. Boil water
          2. Add rice
          """;

      Recipe result = service.importFromText(text, col.getId());

      assertThat(result.getInstructions()).hasSize(2);
      assertThat(result.getInstructions().get(0).getText()).isEqualTo("Boil water");
    }
  }

  // ==================== scaleRecipe ====================

  @Nested
  @DisplayName("scaleRecipe")
  class ScaleRecipe {

    @Test
    @DisplayName("throws IllegalArgumentException when targetServings is zero")
    void throwsForZeroServings() {
      assertThatThrownBy(() -> service.scaleRecipe("any", 0))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("throws IllegalArgumentException when targetServings is negative")
    void throwsForNegativeServings() {
      assertThatThrownBy(() -> service.scaleRecipe("any", -3))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("throws IllegalArgumentException BEFORE checking recipe existence for bad servings")
    void checksServingsBeforeRepo() {
      // If targetServings is invalid, we should never hit the repository
      assertThatThrownBy(() -> service.scaleRecipe("missing-id", 0))
          .isInstanceOf(IllegalArgumentException.class);
      verify(recipeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("throws RecipeNotFoundException when recipe does not exist")
    void throwsWhenRecipeNotFound() {
      when(recipeRepository.findById("bad-id")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.scaleRecipe("bad-id", 4))
          .isInstanceOf(RecipeNotFoundException.class);
    }

    @Test
    @DisplayName("throws IllegalArgumentException when recipe has no servings")
    void throwsWhenNoServings() {
      Recipe recipe = createRecipeNoServings("r-1", "No Servings Recipe");
      when(recipeRepository.findById("r-1")).thenReturn(Optional.of(recipe));

      assertThatThrownBy(() -> service.scaleRecipe("r-1", 4))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("scales ingredient quantities correctly")
    void scalesIngredients() {
      Recipe recipe =
          createRecipe("r-1", "Cookies", 12, List.of(measured("flour", 2.0, Unit.CUP)));
      givenRecipesExist(recipe);

      Recipe scaled = service.scaleRecipe("r-1", 6);

      MeasuredIngredient flour = (MeasuredIngredient) scaled.getIngredients().get(0);
      assertThat(flour.getQuantity().toDecimal()).isEqualTo(1.0); // 2.0 * (6/12) = 1.0
      assertThat(flour.getQuantity().getUnit()).isEqualTo(Unit.CUP);
    }

    @Test
    @DisplayName("scaled recipe has correct servings")
    @SuppressWarnings("NullAway")
    void scaledRecipeHasCorrectServings() {
      Recipe recipe = createRecipe("r-1", "Cookies", 12);
      givenRecipesExist(recipe);

      Recipe scaled = service.scaleRecipe("r-1", 24);

      assertThat(scaled.getServings()).isNotNull();
      assertThat(scaled.getServings().getAmount()).isEqualTo(24);
    }

    @Test
    @DisplayName("scaled recipe gets a new ID (original unchanged)")
    void scaledRecipeHasNewId() {
      Recipe recipe = createRecipe("r-1", "Cookies", 12);
      givenRecipesExist(recipe);

      Recipe scaled = service.scaleRecipe("r-1", 6);

      assertThat(scaled.getId()).isNotEqualTo("r-1");
      assertThat(scaled.getId()).isNotEmpty(); // auto-generated UUID
    }

    @Test
    @DisplayName("scaled recipe is saved to the repository")
    @SuppressWarnings("NullAway")
    void savedToRepository() {
      Recipe recipe = createRecipe("r-1", "Cookies", 12);
      givenRecipesExist(recipe);

      service.scaleRecipe("r-1", 24);

      ArgumentCaptor<Recipe> captor = forClass(Recipe.class);
      verify(recipeRepository).save(captor.capture());
      assertThat(captor.getValue().getServings().getAmount()).isEqualTo(24);
    }

    @Test
    @DisplayName("scaling factor computed correctly for doubling")
    void doublesIngredientAmounts() {
      Recipe recipe =
          createRecipe("r-1", "Soup", 4, List.of(measured("water", 4.0, Unit.CUP)));
      givenRecipesExist(recipe);

      Recipe scaled = service.scaleRecipe("r-1", 8);

      MeasuredIngredient water = (MeasuredIngredient) scaled.getIngredients().get(0);
      assertThat(water.getQuantity().toDecimal()).isEqualTo(8.0);
    }
  }

  // ==================== convertRecipe ====================

  @Nested
  @DisplayName("convertRecipe")
  class ConvertRecipe {

    @Test
    @DisplayName("throws RecipeNotFoundException when recipe does not exist")
    void throwsWhenRecipeNotFound() {
      when(recipeRepository.findById("bad")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.convertRecipe("bad", Unit.MILLILITER))
          .isInstanceOf(RecipeNotFoundException.class);
    }

    @Test
    @DisplayName("converted recipe is saved to repository")
    void savedToRepository() throws UnsupportedConversionException {
      Recipe recipe = createRecipe("r-1", "Soup", 4, List.of(measured("water", 1.0, Unit.CUP)));
      givenRecipesExist(recipe);
      // conversionRegistry will be called by Recipe.convert → MeasuredIngredient.tryConvert
      // We stub it to return a converted quantity
      app.cookyourbooks.model.Quantity convertedQty =
          new ExactQuantity(240.0, Unit.MILLILITER);
      when(conversionRegistry.convert(any(), any(), any())).thenReturn(convertedQty);

      service.convertRecipe("r-1", Unit.MILLILITER);

      verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("converted recipe has a new ID")
    void convertedRecipeHasNewId() throws UnsupportedConversionException {
      Recipe recipe = createRecipe("r-1", "Soup", 4, List.of());
      givenRecipesExist(recipe);

      Recipe converted = service.convertRecipe("r-1", Unit.MILLILITER);

      assertThat(converted.getId()).isNotEqualTo("r-1");
    }

    @Test
    @DisplayName("propagates UnsupportedConversionException from ConversionRegistry")
    void propagatesUnsupportedConversionException() throws UnsupportedConversionException {
      Recipe recipe = createRecipe("r-1", "Soup", 4, List.of(measured("water", 1.0, Unit.CUP)));
      givenRecipesExist(recipe);
      when(conversionRegistry.convert(any(), any(), any()))
          .thenThrow(UnsupportedConversionException.forIngredient(Unit.CUP, Unit.WHOLE, "water"));

      assertThatThrownBy(() -> service.convertRecipe("r-1", Unit.WHOLE))
          .isInstanceOf(UnsupportedConversionException.class);
    }

    @Test
    @DisplayName("recipe title is preserved after conversion")
    void preservesTitle() throws UnsupportedConversionException {
      Recipe recipe = createRecipe("r-1", "My Soup", 4, List.of(measured("water", 1.0, Unit.CUP)));
      givenRecipesExist(recipe);
      when(conversionRegistry.convert(any(), any(), any()))
          .thenReturn(new ExactQuantity(240.0, Unit.MILLILITER));

      Recipe converted = service.convertRecipe("r-1", Unit.MILLILITER);

      assertThat(converted.getTitle()).isEqualTo("My Soup");
    }
  }

  // ==================== generateShoppingList ====================

  @Nested
  @DisplayName("generateShoppingList")
  class GenerateShoppingList {

    @Test
    @DisplayName("combines ingredients with the same name and unit across recipes")
    void combinesLikeIngredients() {
      Recipe cookies =
          createRecipe(
              "rec-cookies",
              "Chocolate Chip Cookies",
              12,
              List.of(measured("flour", 2, Unit.CUP), measured("sugar", 1, Unit.CUP)));
      Recipe cake =
          createRecipe(
              "rec-cake",
              "Chocolate Cake",
              8,
              List.of(measured("flour", 3, Unit.CUP), measured("sugar", 2, Unit.CUP)));
      givenRecipesExist(cookies, cake);

      ShoppingList list = service.generateShoppingList(List.of("rec-cookies", "rec-cake"));

      // Should combine: flour 2+3=5 cups, sugar 1+2=3 cups
      assertThat(list.getItems()).hasSize(2);
      ShoppingItem flour = list.getItems().get(0);
      assertThat(flour.getName()).isEqualTo("flour");
      assertThat(flour.getQuantity().toDecimal()).isEqualTo(5.0);
      assertThat(flour.getQuantity().getUnit()).isEqualTo(Unit.CUP);

      ShoppingItem sugar = list.getItems().get(1);
      assertThat(sugar.getName()).isEqualTo("sugar");
      assertThat(sugar.getQuantity().toDecimal()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("keeps ingredients with the same name but different units as separate items")
    void keepsDifferentUnitsSeparate() {
      Recipe recipeA =
          createRecipe("rec-a", "Recipe A", 4, List.of(measured("butter", 4, Unit.TABLESPOON)));
      Recipe recipeB =
          createRecipe("rec-b", "Recipe B", 4, List.of(measured("butter", 1, Unit.CUP)));
      givenRecipesExist(recipeA, recipeB);

      ShoppingList list = service.generateShoppingList(List.of("rec-a", "rec-b"));

      // butter in TABLESPOON and butter in CUP should NOT be combined
      assertThat(list.getItems()).hasSize(2);

      ShoppingItem butTbsp = list.getItems().get(0);
      assertThat(butTbsp.getName()).isEqualTo("butter");
      assertThat(butTbsp.getQuantity().getUnit()).isEqualTo(Unit.TABLESPOON);
      assertThat(butTbsp.getQuantity().toDecimal()).isEqualTo(4.0);

      ShoppingItem butCup = list.getItems().get(1);
      assertThat(butCup.getName()).isEqualTo("butter");
      assertThat(butCup.getQuantity().getUnit()).isEqualTo(Unit.CUP);
      assertThat(butCup.getQuantity().toDecimal()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("places VagueIngredients in uncountable list")
    void vagueIngredientsGoToUncountable() {
      Recipe recipe =
          createRecipe(
              "rec-1",
              "Recipe",
              4,
              List.of(measured("flour", 2, Unit.CUP), vague("salt")));
      givenRecipesExist(recipe);

      ShoppingList list = service.generateShoppingList(List.of("rec-1"));

      assertThat(list.getItems()).hasSize(1);
      assertThat(list.getItems().get(0).getName()).isEqualTo("flour");
      assertThat(list.getUncountableItems()).containsExactly("salt");
    }

    @Test
    @DisplayName("deduplicates vague ingredients by name (case-insensitive)")
    void deduplicatesVagueIngredients() {
      Recipe r1 = createRecipe("r-1", "A", 4, List.of(vague("salt")));
      Recipe r2 = createRecipe("r-2", "B", 4, List.of(vague("Salt")));
      givenRecipesExist(r1, r2);

      ShoppingList list = service.generateShoppingList(List.of("r-1", "r-2"));

      assertThat(list.getUncountableItems()).hasSize(1);
    }

    @Test
    @DisplayName("returns empty shopping list for empty recipe list")
    void emptyForEmptyInput() {
      ShoppingList list = service.generateShoppingList(List.of());

      assertThat(list.getItems()).isEmpty();
      assertThat(list.getUncountableItems()).isEmpty();
    }

    @Test
    @DisplayName("throws RecipeNotFoundException when any recipe is not found")
    void throwsWhenRecipeNotFound() {
      Recipe r1 = createRecipe("r-1", "Good", 4);
      givenRecipesExist(r1);
      when(recipeRepository.findById("bad")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.generateShoppingList(List.of("r-1", "bad")))
          .isInstanceOf(RecipeNotFoundException.class);
    }

    @Test
    @DisplayName("single recipe with a single ingredient produces one item")
    void singleIngredientSingleRecipe() {
      Recipe recipe = createRecipe("r-1", "Cake", 4, List.of(measured("flour", 3, Unit.CUP)));
      givenRecipesExist(recipe);

      ShoppingList list = service.generateShoppingList(List.of("r-1"));

      assertThat(list.getItems()).hasSize(1);
      assertThat(list.getItems().get(0).getName()).isEqualTo("flour");
      assertThat(list.getItems().get(0).getQuantity().toDecimal()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("ingredient order reflects first-encounter order across recipes")
    void preservesEncounterOrder() {
      Recipe r1 = createRecipe("r-1", "A", 4, List.of(measured("flour", 1, Unit.CUP)));
      Recipe r2 = createRecipe("r-2", "B", 4, List.of(measured("sugar", 2, Unit.CUP)));
      givenRecipesExist(r1, r2);

      ShoppingList list = service.generateShoppingList(List.of("r-1", "r-2"));

      assertThat(list.getItems().get(0).getName()).isEqualTo("flour");
      assertThat(list.getItems().get(1).getName()).isEqualTo("sugar");
    }

    @Test
    @DisplayName("name matching for deduplication is case-insensitive")
    void nameMatchingCaseInsensitive() {
      Recipe r1 = createRecipe("r-1", "A", 4, List.of(measured("Flour", 1, Unit.CUP)));
      Recipe r2 = createRecipe("r-2", "B", 4, List.of(measured("flour", 2, Unit.CUP)));
      givenRecipesExist(r1, r2);

      ShoppingList list = service.generateShoppingList(List.of("r-1", "r-2"));

      // flour and Flour in CUP should be combined
      assertThat(list.getItems()).hasSize(1);
      assertThat(list.getItems().get(0).getQuantity().toDecimal()).isEqualTo(3.0);
    }
  }

  // ==================== findByIngredient ====================

  @Nested
  @DisplayName("findByIngredient")
  class FindByIngredient {

    @Test
    @DisplayName("returns recipes containing a measured ingredient by name (case-insensitive)")
    void findsMeasuredIngredient() {
      Recipe r1 = createRecipe("r-1", "Flour Cake", 4, List.of(measured("Flour", 2, Unit.CUP)));
      Recipe r2 = createRecipe("r-2", "Sugar Cookies", 4, List.of(measured("Sugar", 1, Unit.CUP)));
      when(recipeRepository.findAll()).thenReturn(List.of(r1, r2));

      List<Recipe> results = service.findByIngredient("flour");

      assertThat(results).containsExactly(r1);
    }

    @Test
    @DisplayName("performs substring match on ingredient names")
    void substringMatch() {
      Recipe r1 =
          createRecipe("r-1", "R1", 4, List.of(measured("baking soda", 1, Unit.TEASPOON)));
      Recipe r2 = createRecipe("r-2", "R2", 4, List.of(measured("sodalite", 1, Unit.GRAM)));
      when(recipeRepository.findAll()).thenReturn(List.of(r1, r2));

      List<Recipe> results = service.findByIngredient("soda");

      assertThat(results).containsExactlyInAnyOrder(r1, r2);
    }

    @Test
    @DisplayName("returns empty list when no recipes match")
    void emptyWhenNoMatch() {
      Recipe r1 = createRecipe("r-1", "Cake", 4, List.of(measured("flour", 2, Unit.CUP)));
      when(recipeRepository.findAll()).thenReturn(List.of(r1));

      List<Recipe> results = service.findByIngredient("chocolate");

      assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("returns empty list when repository has no recipes")
    void emptyRepositoryReturnsEmpty() {
      when(recipeRepository.findAll()).thenReturn(List.of());

      List<Recipe> results = service.findByIngredient("flour");

      assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("finds recipes with vague ingredients by name")
    void findsVagueIngredients() {
      Recipe r1 = createRecipe("r-1", "Spiced Dish", 4, List.of(vague("pepper")));
      Recipe r2 = createRecipe("r-2", "Bland Dish", 4, List.of(vague("water")));
      when(recipeRepository.findAll()).thenReturn(List.of(r1, r2));

      List<Recipe> results = service.findByIngredient("pepper");

      assertThat(results).containsExactly(r1);
    }

    @Test
    @DisplayName("search is case-insensitive for query")
    void caseInsensitiveSearch() {
      Recipe r1 = createRecipe("r-1", "Cake", 4, List.of(measured("Flour", 2, Unit.CUP)));
      when(recipeRepository.findAll()).thenReturn(List.of(r1));

      List<Recipe> results = service.findByIngredient("FLOUR");

      assertThat(results).containsExactly(r1);
    }
  }
}
