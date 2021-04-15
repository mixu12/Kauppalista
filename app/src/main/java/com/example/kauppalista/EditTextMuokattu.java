package com.example.kauppalista;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class EditTextMuokattu extends EditText{
    public EditTextMuokattu(Context context )
    {
        super( context );
    }

    public EditTextMuokattu(Context context, AttributeSet attribute_set )
    {
        super( context, attribute_set );
    }

    public EditTextMuokattu(Context context, AttributeSet attribute_set, int def_style_attribute )
    {
        super( context, attribute_set, def_style_attribute );
    }

    @Override
    public boolean onKeyPreIme( int key_code, KeyEvent event )
    {
        if ( key_code == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
            this.clearFocus();

        return super.onKeyPreIme( key_code, event );
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean liitetty = super.onTextContextMenuItem(id);

        switch (id){
            case android.R.id.paste:
                liitetty();
                break;
        }
        return liitetty;
    }

    public void liitetty(){
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    }
}