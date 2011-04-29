A Gentle Introduction To Logic Programming
----

This tutorial will guide you through the magic and fun of relational programming. This tutorial does not assume that you have any knowledge of Lisp, Clojure, Java, or even Functional Programming. The only thing this tutorial assumes is that you are not afraid of using the command line and you’ve used some programming language before in your life. Since you’re using GitHub I think this is a relatively safe assumption :)

First things first, install Leiningen or Cake. Then clone this repository and switch into its directory. Once you’ve done that, run lein deps or cake deps. This will grab all of the dependencies needed for this tutorial.

Getting Started
____

Ok, we’re ready to being. Type lein repl or cake repl, this will drop you into the Clojure prompt. Enter the following at the Clojure REPL:

```sh
user=>(require ‘clojure.core.logic.minikanren)
```

The REPL should print nil and it should return control to you. If it doesn’t file an issue for this tutorial and I’ll look into it. If all goes well run the following:

```sh
user=>(load “logic_tutorial/tutorial1”)
```

You’ll see some harmless warning, then run the following:

```sh
(in-ns ‘logic-tutorial.tutorial1)
```

Your prompt will change and you’re now working in a place that has the magic of logic programming available to you.

Baby Steps
----

Unlike most programming systems, with relational programming we can actually ask the computer questions. But before we ask the computer questions, we need define some facts! The first thing we want the computer to know about is that there are men:

user=>(defrel man x)
#'logic-tutorial.tutorial1/man

And then we want to define some men:

user=>(fact man ‘Bob)
nil
user=>(fact man ‘John)
nil

No we can ask who are men. First we have to formulate a question and then tell the computer we want the computer to find answers to our question:

user=> (run 1 [q] (man q))
(John)

We’re asking the computer to give us at least one answer to the question - “Who is a man?”.  We can ask for more than one answer:

user=>(run 2 [q] (man q))
(John Bob)

Now that is pretty cool. What happens if we ask for more answers?

user=>(run 3 [q] (man q))
(John Bob)

The same result. That’s because we’ve only told the computer that two men exist in the world. It can’t give results for things it doesn’t know about. Let’s define another kind of relationship and a fact:

user=>(defrel fun x)
#'logic-tutorial.tutorial1/fun
user=>(fact fun ‘Bob)
nil

Let’s ask a new kind of question:

user=>(run* [q] (man q) (fun q))
(Bob)

There’s a couple of new things going on here. We’re asking who is both a man and who is fun. Now this getting interesting. Enter in the following:

user=>(defrel woman x)
#’logic-tutorial.tutorial1/woman
user=>(fact woman ‘Lucy)
nil
user=>(fact woman ‘Mary)
nil
user=>(defrel likes x y)
#’logic-tutorial.tutorial1/likes

Relations don’t have to be a about a single entity. We can define relationship between things!

user=>(fact likes ‘Bob ‘Mary)
nil
user=>(fact likes ‘John ‘Lucy)
nil
user=>(run* [q] (likes ‘Bob q))
(Mary)

We can now ask who likes who! Let’s try this:

user=>(run* [q] (likes ‘Mary q))
()

Hmm that doesn’t work. This is because we never actually said who Mary liked, only that Bob liked Mary:

user=>(fact likes ‘Mary ‘Bob)
nil

user=>(run* [q] (exist [x y] (== q [x y]) (likes x y) ))
([Bob Mary] [John Lucy])

Wow that’s a lot of new information. The exist expression isn’t something we’ve seen before. Why do we need it? That’s because by convention run returns single values. In this case we want to know who like who. This means we need to create to logic variables to store these values in. We then assign both these values to q.

I’ve done a lot of lying in the last paragraph. Run the following:

user=>(run* [q] (exist [x y] (likes x y) (== q [x y])))
([Bob Mary] [John Lucy])

Note that the order doesn’t not matter. We can call the like relation and then assign them to q or we can assign to q and call the like relation. But really this isn’t assigment. This is a fairly powerful notion called unification.

We’ve actually defined some interesting relations in this namespace that we’ll use before we take a closer look at them:

user=>(fact parent 'John 'Bobby)
nil
user=>(fact male ‘Bobby)
nil

We can now run this query:

user=>(run* [q] (son q 'John))
(Bobby)

Let’s add another fact:

user=>(fact parent 'George 'John) 
nil
user=>(run* [q] (grandparent q 'Bobby))
(George)

Huzzah! We define relations in terms of other relations! Composition to the rescue.

Let’s take a moment to look at what’s in the file.

By now we’re tired of genealogy. Let’s go back to the cozy world of Computer Science. One of the very first things people introduce in CS are arrays and/or lists. It’s often convenient to take two lists and join them together. We’ll definie a relational function that does this for us.

There’s actually a short hand for writing appendo, we can write it like this. This is pattern matching - it can decrease the amount of boiler plate we have to write for many programs.