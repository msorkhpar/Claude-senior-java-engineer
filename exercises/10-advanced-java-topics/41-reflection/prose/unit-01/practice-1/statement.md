The platform team's event bus delivers events to listener methods marked `@Subscribe`. Three
bug reports have come in since it went live: the cart-recovery listener never hears about
abandoned carts, the onboarding listener's audit trail is empty although its base class
subscribes to sign-ups, and the refunds listener, which implements a generic `Listener<E>`
interface, receives each refund twice and blows up when a cart event is published.

Registration, delivery and the annotation already work. Finish `subscriberMethods(...)` in
`src/main/java/practice/EventBus.java`: given a listener's class, return the methods that should
receive events.

When you're done, every method a listener's author marked with `@Subscribe` and gave one
parameter receives its events exactly once, whatever its access and whether it is declared in the
listener's class or in one of its superclasses. Listeners never override a subscriber method of
their superclass. About eight lines in total.
