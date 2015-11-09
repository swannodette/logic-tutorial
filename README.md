A Very Gentle Introduction To Relational & Functional Programming
====

This tutorial will guide you through the magic and fun of combining relational programming (also known as logic programming) with functional programming. This tutorial does not assume that you have any knowledge of Lisp, Clojure, Java, or even functional programming. The only thing this tutorial assumes is that you are not afraid of using the command line and you have used at least one programming language before in your life.

Work in Progress
----

This tutorial is very much a work in progress. It's possible to get through the first parts and learn something, but expect a considerable amount of fleshing out in the next couple of weeks.

Why Logic Programming?
----

What's the point of writing programs in the relational paradigm? First off, aesthetics dammit.

Logic programs are simply beautiful as they often have a declarative nature which trumps even the gems found in functional programming languages. Logic programs use search, and thus they are often not muddied up by algorithmic details. If you haven't tried Prolog before, relational programming will at times seem almost magical.

However, I admit, the most important reason to learn the relational paradigm is because it's FUN.

First Steps
----

1. Install `lein` following [instructions here](http://leiningen.org/#install)
2. `git clone https://github.com/swannodette/logic-tutorial && cd logic-tutorial`

Ok, we're ready to begin. Type `lein repl`, which will drop you into the Clojure prompt. First let's double check that everything went ok. Enter the following at the Clojure REPL:

```clj
user=> (require 'clojure.core.logic)
```

The REPL should print `nil` and it should return control to you. If it doesn't file an issue for this tutorial and I'll look into it. If all goes well run the following:

```clj
user=> (load "logic_tutorial/tut1")
```

You'll see some harmless warnings, then run the following:

```clj
user=> (in-ns 'logic-tutorial.tut1)
```

Your prompt will change and you're now working in a place that has the magic of relational programming available to you. The REPL prompt will show `logic-tutorial.tut1`, we're going show `tut1` to keep things concise.

Question & Answer
----

Unlike most programming systems, with relational programming we can actually ask the computer questions. But before we ask the computer questions, we need define some facts! The first thing we want the computer to know about is that there are men:

```clj
tut1=> (db-rel man x)
#'tut1/man
```

And then we want to define some men:

```clj
tut1=> (def men
         (db
           [man 'Bob]
           [man 'John]))
nil
```

Now we can ask who are men. Questions are always asked with `run` or `run*`. By convention we'll declare a logic variable `q` and ask the computer to give use the possible values for `q`. Here's an example.

```clj
tut1=> (with-db men
         (run 1 [q] (man q)))
(John)
```

We're asking the computer to give us at least one answer to the question - "Who is a man?".  We can ask for more than one answer:

```clj
tut1=> (with-db men
         (run 2 [q] (man q)))
(John Bob)
```

Now that is pretty cool. What happens if we ask for even more answers?

```clj
tut1=> (with-db men
         (run 3 [q] (man q)))
(John Bob)
```

The same result. That's because we’ve only told the computer that two men exist in the world. It can't give results for things it doesn't know about. Let's define another kind of relationship and a fact:

```clj
tut1=> (db-rel fun x)
#'tut1/fun
tut1=> (def fun-people
         (db
           [fun 'Bob]))
nil
```

Let's ask a new kind of question:

```clj
tut1=> (with-dbs [men fun-people]
         (run* [q]
           (man q)
           (fun q)))
(Bob)
```

There's a couple of new things going on here. We use `run*`. This means we want all the answers the computer can find. The question itself is formulated differently than before because we're asking who is a man *and* is fun. Enter in the following:

```clj
tut1=> (db-rel woman x)
#'tut1/woman
tut1=> (def facts
         (db
           [woman 'Lucy]
           [woman 'Mary]))
nil
tut1=> (db-rel likes x y)
#'tut1/likes
```

We have now switched to a more generic name for the database of 'facts', which
we will expand with facts about different relations. Relations don't have to be about a single entity. We can define relationship between things!

```clj
tut1=> (def facts
         (-> facts
           (db-fact likes 'Bob 'Mary)
           (db-fact likes 'John 'Lucy)))
nil
tut1=> (with-dbs [men facts] (run* [q] (likes 'Bob q)))
(Mary)
```

We've added two facts to the 'facts' database and can now ask who likes who!

However, let's try this:

```clj
tut1=> (with-dbs [men facts] (run* [q] (likes 'Mary q)))
()
```

Hmm that doesn't work. This is because we never actually said *who Mary liked*, only that Bob liked Mary. Try the following:

```clj
tut1=> (def facts (db-fact facts likes 'Mary 'Bob))
nil
tut1=> (with-dbs [men facts] (run* [q] (fresh [x y] (likes x y) (== q [x y]))))
([Mary Bob] [Bob Mary] [John Lucy])
```

Wow that's a lot of new information. The fresh expression isn't something we've seen before. Why do we need it? By convention `run` returns single values for `q` which answer the question. In this case we want to know who likes who. This means we need to create logic variables to store these values in. We then assign both these values to `q` by putting them in a Clojure vector (which is like an array in other programming languages).

Try the following:

```clj
tut1=> (with-dbs [men facts] (run* [q] (fresh [x y] (likes x y) (likes y x) (== q [x y]))))
([Mary Bob] [Bob Mary])
```

Here we only want the list of people who like each other. Note that the order of how we pose our question doesn't matter:

```clj
tut1=> (with-dbs [men facts] (run* [q] (fresh [x y] (likes x y) (== q [x y]) (likes y x))))
([Mary Bob] [Bob Mary])
```

Some Genealogy
----

We've actually predefined some interesting relations in the `tut1` file that we'll try out first before we take a closer look:

```clj
tut1=> (def genealogy
         (db
           [parent 'John 'Bobby]
           [male 'Bobby]))
nil
```
We can now run this query:

```clj
tut1=> (with-db genealogy
         (run* [q]
           (son q 'John)))
(Bobby)
```

Let's add another fact:

```clj
tut1=> (def genealogy
         (-> genealogy
           (db-fact parent 'George 'John)))
nil
tut1=> (with-db genealogy (run* [q] (grandparent q 'Bobby)))
(George)
```

Huzzah! We can define relations in terms of other relations! Composition to the rescue. But how does this work exactly?

Let's take a moment to look at what's in the file. At the top of the file after the namespace declaration you'll see that some relations have been defined:

```clj
(db-rel parent x y)
(db-rel male x)
(db-rel female x)
```

After this there are some functions:

```clj
(defn child [x y]
  (parent y x))

(defn son [x y]
  (all
    (child x y)
    (male x)))

(defn daughter [x y]
  (all
    (child x y)
    (female x)))
```

We can define relations as functions! Play around with defining some new facts and using these relations to pose questions about these facts. If you're feeling particularly adventurous, write a new relation and use it.

Primitives
----

Let's step back for a moment. `core.logic` is built upon a small set of primitives - they are `run`, `fresh`, `==`, and `conde`. We're already pretty familiar with `run`, `fresh`, and `==`. `run` is simple, it lets us `run` our logic programs. `fresh` is also pretty simple, it lets us declare new logic variables. `==` is a bit mysterious and we've never even seen `conde` before.

Unification
----

Earlier I lied about assignment when using the `==` operator. The `==` operator means that we want to unify two terms. This means we'd like the computer to take two things and attempt to make them equal. If logic variables occur in either of the terms, the computer will try to bind that logic variable to what ever value matches in the other term. If the computer can't make two terms equal, it fails - this is why sometimes we don't see any results.

Consider the following:

```clj
tut1=> (run* [q] (== 5 5))
(_0)
```

Whoa, what does that mean? It means that our question was fine, but that we never actually unified `q` with anything - `_0` just means we have a logic variable that was never bound to a concrete value.

```clj
tut1=> (run* [q] (== 5 4))
()
```

It's impossible to make 5 and 4 equal to each other, the computer lets us know that no successful answers exist for the question we posed.

```clj
tut1=> (run* [q] (== q 5))
(5)
tut1=> (run* [q] (== q 5) (== q 4))
()
```

Once we've unified a logic variable to a concrete value we can unify it again with that value, but we cannot unify with a concrete value that is not equal to what it is currently bound to.

Here's an example showing that we can unify complex terms:

```clj
tut1=> (run* [q] (fresh [x y] (== [x 2] [1 y]) (== q [x y])))
([1 2])
```

This shows that in order for the two terms `[x 2]` and `[1 y]` to be unified, the logic variable `x` must be bound to 1 and the logic variable `y` must be bound to 2.

Note: it's perfectly fine to unify two variables to each other:

```clj
tut1=> (run* [q] (fresh [x y] (== x y) (== q [x y])))
([_0 _0])
tut1=> (run* [q] (fresh [x y] (== x y) (== y 1) (== q [x y])))
([1 1])
```

Multiple Universes
----

By now we're already familiar with conjunction, that is, logical **and**.

```clj
(with-dbs [facts fun-people] (run* [q] (fun q) (likes q 'Mary)))
```

We know now to read this as find `q` such that `q` is fun **and** `q` likes Mary.

But how to express logical **or**?

```clj
(with-dbs [facts fun-people]
  (run* [q]
    (conde
      ((fun q))
      ((likes q 'Mary)))))
```

The above does exactly that - find `q` such that `q` is fun *or* `q` likes Mary. This is the essence of how we get multiple answers from `core.logic`.

Magic Tricks
----

By now we're tired of genealogy. Let's go back to the cozy world of Computer Science. One of the very first things people introduce in CS are arrays and/or lists. It’s often convenient to take two lists and join them together. In Clojure this functionality exists via `concat`. However we're going to look at a relational version of the function called `appendo`. While `appendo` is certainly slower than `concat` it has magical powers that `concat` does not have.

First we'll want to load the next tutorial and switch into its namespace.

Note: Since `core.logic` 0.6.3, `appendo` has been included in `core.logic` itself.

```clj
tut1=> (in-ns 'user)
nil
user=> (load "logic_tutorial/tut2")
nil
user=> (in-ns 'logic-tutorial.tut2)
nil
```

Relational functions are written quite differently than their functional counterparts. Instead of return value, we usually make the final parameter be output variable that we'll unify the answer to. This makes it easier to compose relations together. This also means that relational programs in general look quite different from functional programs.

Open `src/logic-tutorial/tut2.clj`. You'll find the definition for `appendo`.

```clj
(defn appendo [l1 l2 o]
  (conde
    ((== l1 ()) (== l2 o))
    ((fresh [a d r]
       (conso a d l1)
       (conso a r o)
       (appendo d l2 r)))))
```

We can pass in logic variables in any one of its three arguments.

Now try the following:

```clj
tut2=> (run* [q] (appendo [1 2] [3 4] q))
((1 2 3 4))
```

Seems reasonable. Now try this:

```clj
tut2=> (run* [q] (appendo [1 2] q [1 2 3 4]))
((3 4))
```

Note that `appendo` can infer its inputs!

There’s actually a short hand for writing appendo, we can write it like this. This is pattern matching - it can decrease the amount of boiler plate we have to write for many programs.

Zebras
----

There's a classic old puzzle sometimes referred to as the Zebra puzzle, sometimes as Einstein's puzzle. Writing an algorithm for solving the constraint is a bit tedious - relational programming allows us to just describe the constraints and it can produce the correct answer for us.

The puzzle is described in the following manner.

If you look in `src/logic_tutorial/tut3.clj` you'll find the following code:

```clj
(defne righto [x y l]
  ([_ _ [x y . ?r]])
  ([_ _ [_ . ?r]] (righto x y ?r)))

(defn nexto [x y l]
  (conde
    ((righto x y l))
    ((righto y x l))))

(defn zebrao [hs]
  (macro/symbol-macrolet [_ (lvar)]
    (all
     (== [_ _ [_ _ 'milk _ _] _ _] hs)
     (firsto hs ['norwegian _ _ _ _])
     (nexto ['norwegian _ _ _ _] [_ _ _ _ 'blue] hs)
     (righto [_ _ _ _ 'ivory] [_ _ _ _ 'green] hs)
     (membero ['englishman _ _ _ 'red] hs)
     (membero [_ 'kools _ _ 'yellow] hs)
     (membero ['spaniard _ _ 'dog _] hs)
     (membero [_ _ 'coffee _ 'green] hs)
     (membero ['ukrainian _ 'tea _ _] hs)
     (membero [_ 'lucky-strikes 'oj _ _] hs)
     (membero ['japanese 'parliaments _ _ _] hs)
     (membero [_ 'oldgolds _ 'snails _] hs)
     (nexto [_ _ _ 'horse _] [_ 'kools _ _ _] hs)
     (nexto [_ _ _ 'fox _] [_ 'chesterfields _ _ _] hs))))
```

That is the entirety of the program. Let's run it:

```clj
tut3=> (run 1 [q] (zebrao q))
([[norwegian kools _.0 fox yellow] [ukrainian chesterfields tea horse blue] [englishman oldgolds milk snails red] [spaniard lucky-strikes oj dog ivory] [japanese parliaments coffee _.1 green]])
```

But how fast is it?

```clj
tut3=> (dotimes [_ 100] (time (doall (run 1 [q] (zebrao q)))))
```

On my machine, after the JVM has had time to warm up, I see the puzzle can be solved in as little as 3 milliseconds. The Zebra puzzle in and of itself is hardly very interesting. However if such complex constraints can be described and solved so quickly, `core.logic` is very likely fast enough to be applied to reasoning about types! Only time will tell, but I encourage people to investigate such applications.

Next Steps
----

Hopefully this short tutorial has revealed some of the beauty of relational programming. To be sure, relational programming as I've presented here has its limitations. Yet, people are actively working on surmounting those limitations in more ways than I really have time to document here.

While you can get along just fine as a programmer without using relational programming, many aspects of the tools we use today will seem mysterious without a basic understanding of how relational programming works. It also allows us to add features to our languages that are otherwise harder to implement. For example the elegant type systems (and type inferencing) found in Standard ML and Haskell would be fascinating to model via `core.logic`. I also think that an efficient predicate dispatch system that gives ML pattern matching performance with the open-ended nature of CLOS generic methods would be easily achievable via `core.logic`.

Resources
---

If you found this tutorial interesting and would like to learn more I recommend the following books to further you understanding of the relational paradigm.

* [The Reasoned Schemer](http://mitpress.mit.edu/catalog/item/default.asp?ttype=2&tid=10663)
* [Paradigms of Artificial Intelligence Programming](http://norvig.com/paip.html)
* [Prolog Programming For Artificial Intelligence](http://www.amazon.com/Prolog-Programming-Artificial-Intelligence-Bratko/dp/0201403757)
* [Concepts, Techniques, and Models of Computer Programming](http://www.info.ucl.ac.be/~pvr/book.html)

