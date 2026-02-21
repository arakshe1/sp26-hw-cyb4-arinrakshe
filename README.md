## CookYourBooks Assignment 4: RecipeService and Testing

Welcome to the **CookYourBooks** project! Over the course of the semester, you'll be building a
comprehensive recipe management application that helps users digitize, organize, and work with their
recipe collections. This application will eventually support importing recipes from various sources
(including OCR from photos), storing them in a structured format, and providing both command-line
and graphical interfaces for managing a personal recipe library.

In this assignment, you'll build **`RecipeService`** — the application layer that sits between user interfaces (CLI, GUI) and your domain model. This facade coordinates everything: parsing recipe text, transforming quantities, persisting to repositories, and aggregating shopping lists. It's the "brain" that the CLI (A5) might call to get things done.

Read the complete, up-to-date specification for this assignment
[on the course website](https://neu-pdi.github.io/cs3100-public-resources/assignments/cyb4-testing).
