package me.tommymyers.utils.simplecommands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandHandler {
    public String name();
    public String[] aliases() default {};
    public String usage() default "";
    public String description() default "";
    public String permission() default "";
    public String noPermissionMessage() default "\u00A7cYou do not have permission to use this command!";
}
