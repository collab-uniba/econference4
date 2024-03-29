package it.uniba.di.cdg.xcore.ui.preferences;

import it.uniba.di.cdg.xcore.ui.UiPlugin;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into
 * JFace that allows us to create a page that is small and knows how to save, restore and apply
 * itself. <p> This page is used to modify preferences only. They are stored in the preference store
 * that belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 */

public class GeneralPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public static final String AUTO_LOGIN = "it.uniba.di.cdg.xcore.ui.preferences_general";

    private ScopedPreferenceStore preferences;

    public GeneralPreferencePage() {
        super( GRID );
        setDescription( "Connection preferences" );
        preferences = new ScopedPreferenceStore( new ConfigurationScope(), UiPlugin.ID );
        setPreferenceStore( preferences );
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    public void createFieldEditors() {
        // addField( new DirectoryFieldEditor( PreferenceConstants.P_PATH, "&Directory preference:",
        // getFieldEditorParent() ) );
        // addField( new BooleanFieldEditor( PreferenceConstants.P_BOOLEAN,
        // "&An example of a boolean preference", getFieldEditorParent() ) );
        //
        // addField( new RadioGroupFieldEditor( PreferenceConstants.P_CHOICE,
        // "An example of a multiple-choice preference", 1, new String[][] {
        // { "&Choice 1", "choice1" }, { "C&hoice 2", "choice2" } },
        // getFieldEditorParent() ) );
        // addField( new StringFieldEditor( PreferenceConstants.P_STRING, "A &text preference:",
        // getFieldEditorParent() ) );
        BooleanFieldEditor boolEditor = new BooleanFieldEditor( AUTO_LOGIN,
                "Login automatically at startup with the last profile used", getFieldEditorParent() );
        addField( boolEditor );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        try {
            preferences.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.performOk();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init( IWorkbench workbench ) {
        preferences.setDefault( GeneralPreferencePage.AUTO_LOGIN, false );
    }

}