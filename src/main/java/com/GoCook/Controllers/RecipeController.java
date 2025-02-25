package com.GoCook.Controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.GoCook.Boundaries.CategoryDAO;
import com.GoCook.Boundaries.IngredientDAO;
import com.GoCook.Boundaries.RecipeDAO;
import com.GoCook.Entities.Category;
import com.GoCook.Entities.Ingredient;
import com.GoCook.Entities.Recipe;

/**
 * 
 * Controls the endpoints for the Recipes
 * @author 300300914
 *
 */
@Controller
public class RecipeController {
	
	@Autowired
	RecipeDAO rDAO;
	
	@Autowired
	CategoryDAO cDAO;
	
	/**
	 * Maps the /recipes (list of all recipes)
	 * @param model Sends the recipe object to the view to fill the html form
	 * @return the view of Recipes (list)
	 */
	
	@GetMapping("/recipes")
	public String ShowAll(Model model) {
		model.addAttribute("categories", cDAO.getCategories());
		model.addAttribute("category", new Category());
		
		model.addAttribute("recipe", new Recipe());
		return "recipes/recipes";
	}
	
	/**
	 * Makes recipes list available in the view
	 * @return List of recipes
	 */
	@ModelAttribute("recipes")
	public Iterable<Recipe> getAll() {
		return rDAO.getAllRecipes();
	}
	
	/**
	 * Update recipe
	 * @param recipe The recipe entity with an existing id
	 * @return The view/recipe
	 */
	@PutMapping("/recipes")
	public String updateRecipe(@ModelAttribute Recipe recipe) {
		Recipe recipe_db = rDAO.findById(recipe.getId()).get();
		recipe_db.setTitle(recipe.getTitle());
		recipe_db.setImage(recipe.getImage());
		recipe_db.setInstructions(recipe.getInstructions());
		recipe_db.setPrepTime(recipe.getPrepTime());
		recipe_db.setCookTime(recipe.getCookTime());
		recipe_db.setServingQty(recipe.getServingQty());
		recipe_db.setCategories(recipe.getCategories());
		recipe_db.setQty_ingredients(recipe.getQty_ingredients());
		
		rDAO.save(recipe_db);
		return "redirect:/recipes";
	}

	/**
	 * Delete (hide) recipe
	 * @param recipe The recipe entity
	 * @return The view /recipe
	 */
	@DeleteMapping("/recipes")
	public String deleteRecipe(@ModelAttribute Recipe recipe) {
		Recipe recipe_db = rDAO.findById(recipe.getId()).get();
		recipe_db.setActive(false);
		rDAO.save(recipe_db);
		return "redirect:/recipes";
	}
	

	/**
	 * 
	 * @param id The id of the recipe
	 * @param model	The model to be sent to the view
	 * @return the view for a specific recipe
	 */
	@GetMapping("/recipe/{id}/detail")
	public String getRecipe(@PathVariable String id, Model model) {
		model.addAttribute("categories", cDAO.getCategories());
		model.addAttribute("category", new Category());
		
		model.addAttribute("recipe", rDAO.findById(Integer.parseInt(id)).get());
		return "recipes/recipedetails";
	}
	
	
	/**
	 * Return a recipe object from an id
	 * @param id The id of the recipe
	 * @return the JSON object of a recipe
	 */
	@GetMapping("/recipes/{id}")
	@ResponseBody
	public Recipe getObject(@PathVariable String id) {
		try {
			return rDAO.findById(Integer.parseInt(id)).get();
		} catch(Exception ex) {
			return new Recipe();
		}
	}
	
	@Autowired
	IngredientDAO iDao;
	
	@GetMapping("/recipe/search")
	public String getRecipeCor(@RequestParam (value="query") String query, Model model) {
		Iterable<Ingredient> ingredients = iDao.findByNameContains(query);
		List<Recipe> recipesList = new ArrayList<>();
		
		for(Ingredient ingredient : ingredients) {
			System.out.println("Ingredient => " + ingredient);
			Iterable<Recipe> recipes = rDAO.findByIngredient(ingredient);
			recipes.iterator().forEachRemaining(recipesList::add);
		}
		
		List<Recipe> recipesFiltered = new ArrayList<>();
		
		for(Recipe recipe : recipesList) {
			if(!recipesFiltered.contains(recipe))
				recipesFiltered.add(recipe);
		}
		model.addAttribute("categories", cDAO.getCategories());
		model.addAttribute("category", new Category());
		
		model.addAttribute("searchword", query);
		model.addAttribute("recipes", recipesFiltered);
		model.addAttribute("recipe", new Recipe());
		return "recipes/searchresult";
	}

}
