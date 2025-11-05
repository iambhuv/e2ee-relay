import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.promtuz.chat.ui.components.FlexibleScreen
import org.koin.androidx.compose.koinViewModel
import com.promtuz.chat.presentation.viewmodel.${NAME}VM

@Composable
fun ${NAME}Screen(viewModel: ${NAME}VM = koinViewModel()) {
    FlexibleScreen({ Text("${NAME}") }) { padding, scrollBehavior ->
        Text("${NAME} Screen")
    }
}